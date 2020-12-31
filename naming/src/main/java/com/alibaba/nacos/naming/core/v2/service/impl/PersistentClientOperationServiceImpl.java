/*
 *  Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.alibaba.nacos.naming.core.v2.service.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.utils.ClassUtils;
import com.alibaba.nacos.common.utils.Objects;
import com.alibaba.nacos.consistency.DataOperation;
import com.alibaba.nacos.consistency.SerializeFactory;
import com.alibaba.nacos.consistency.Serializer;
import com.alibaba.nacos.consistency.cp.CPProtocol;
import com.alibaba.nacos.consistency.cp.RequestProcessor4CP;
import com.alibaba.nacos.consistency.entity.ReadRequest;
import com.alibaba.nacos.consistency.entity.Response;
import com.alibaba.nacos.consistency.entity.WriteRequest;
import com.alibaba.nacos.consistency.snapshot.LocalFileMeta;
import com.alibaba.nacos.consistency.snapshot.Reader;
import com.alibaba.nacos.consistency.snapshot.SnapshotOperation;
import com.alibaba.nacos.consistency.snapshot.Writer;
import com.alibaba.nacos.core.distributed.ProtocolManager;
import com.alibaba.nacos.naming.consistency.persistent.impl.AbstractSnapshotOperation;
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.client.Client;
import com.alibaba.nacos.naming.core.v2.client.impl.IpPortBasedClient;
import com.alibaba.nacos.naming.core.v2.client.manager.impl.PersistentIpPortClientManager;
import com.alibaba.nacos.naming.core.v2.event.client.ClientOperationEvent;
import com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.core.v2.service.ClientOperationService;
import com.alibaba.nacos.naming.pojo.Subscriber;
import com.alibaba.nacos.naming.utils.Constants;
import com.alibaba.nacos.sys.utils.ApplicationUtils;
import com.alibaba.nacos.sys.utils.DiskUtils;
import com.alipay.sofa.jraft.util.CRC64;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.Checksum;

/**
 * Operation service for persistent clients and services. only for v2 For persistent instances, clientId must be in the
 * format of host:port.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@Component("persistentClientOperationServiceImpl")
public class PersistentClientOperationServiceImpl extends RequestProcessor4CP implements ClientOperationService {
    
    private final PersistentIpPortClientManager clientManager;
    
    private final Serializer serializer = SerializeFactory.getDefault();
    
    private final CPProtocol protocol;
    
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    
    public PersistentClientOperationServiceImpl(final PersistentIpPortClientManager clientManager) {
        this.clientManager = clientManager;
        this.protocol = ApplicationUtils.getBean(ProtocolManager.class).getCpProtocol();
        this.protocol.addRequestProcessors(Collections.singletonList(this));
    }
    
    @Override
    public void registerInstance(Service service, Instance instance, String clientId) {
        final InstanceStoreRequest request = new InstanceStoreRequest();
        request.setService(service);
        request.setInstance(instance);
        request.setClientId(clientId);
        final WriteRequest writeRequest = WriteRequest.newBuilder().setGroup(group())
                .setData(ByteString.copyFrom(serializer.serialize(request))).setOperation(DataOperation.ADD.name())
                .build();
        
        try {
            protocol.write(writeRequest);
        } catch (Exception e) {
            throw new NacosRuntimeException(NacosException.SERVER_ERROR, e);
        }
    }
    
    @Override
    public void deregisterInstance(Service service, Instance instance, String clientId) {
        final InstanceStoreRequest request = new InstanceStoreRequest();
        request.setService(service);
        request.setInstance(instance);
        request.setClientId(clientId);
        final WriteRequest writeRequest = WriteRequest.newBuilder().setGroup(group())
                .setData(ByteString.copyFrom(serializer.serialize(request))).setOperation(DataOperation.DELETE.name())
                .build();
        
        try {
            protocol.write(writeRequest);
        } catch (Exception e) {
            throw new NacosRuntimeException(NacosException.SERVER_ERROR, e);
        }
    }
    
    @Override
    public void subscribeService(Service service, Subscriber subscriber, String clientId) {
        throw new UnsupportedOperationException("No persistent subscribers");
    }
    
    @Override
    public void unsubscribeService(Service service, Subscriber subscriber, String clientId) {
        throw new UnsupportedOperationException("No persistent subscribers");
    }
    
    @Override
    public Response onRequest(ReadRequest request) {
        throw new UnsupportedOperationException("Temporary does not support");
    }
    
    @Override
    public Response onApply(WriteRequest request) {
        final InstanceStoreRequest instanceRequest = serializer.deserialize(request.getData().toByteArray());
        final DataOperation operation = DataOperation.valueOf(request.getOperation());
        final Lock lock = readLock;
        lock.lock();
        try {
            switch (operation) {
                case ADD:
                    onInstanceRegister(instanceRequest.service, instanceRequest.instance,
                            instanceRequest.getClientId());
                    break;
                case DELETE:
                    onInstanceDeregister(instanceRequest.service, instanceRequest.getClientId());
                    break;
                default:
                    return Response.newBuilder().setSuccess(false).setErrMsg("unsupport operation : " + operation)
                            .build();
            }
            return Response.newBuilder().setSuccess(true).build();
        } finally {
            lock.unlock();
        }
    }
    
    private void onInstanceRegister(Service service, Instance instance, String clientId) {
        Service singleton = ServiceManager.getInstance().getSingleton(service);
        Client client = clientManager.computeIfAbsent(clientId, () -> new IpPortBasedClient(clientId, false));
        InstancePublishInfo instancePublishInfo = getPublishInfo(instance);
        client.addServiceInstance(singleton, instancePublishInfo);
        client.setLastUpdatedTime();
        NotifyCenter.publishEvent(new ClientOperationEvent.ClientRegisterServiceEvent(singleton, clientId));
    }
    
    private void onInstanceDeregister(Service service, String clientId) {
        Service singleton = ServiceManager.getInstance().getSingleton(service);
        Client client = clientManager.getClient(clientId);
        client.removeServiceInstance(singleton);
        client.setLastUpdatedTime();
        if (client.getAllPublishedService().isEmpty()) {
            clientManager.clientDisconnected(clientId);
        }
        NotifyCenter.publishEvent(new ClientOperationEvent.ClientDeregisterServiceEvent(singleton, clientId));
    }
    
    @Override
    public List<SnapshotOperation> loadSnapshotOperate() {
        return Collections.singletonList(new PersistentInstanceSnapshotOperation(lock));
    }
    
    @Override
    public String group() {
        return Constants.NAMING_PERSISTENT_SERVICE_GROUP_V2;
    }
    
    protected static class InstanceStoreRequest implements Serializable {
    
        private static final long serialVersionUID = -9077205657156890549L;
    
        private Service service;
        
        private Instance instance;
        
        private String clientId;
        
        public Service getService() {
            return service;
        }
        
        public void setService(Service service) {
            this.service = service;
        }
        
        public Instance getInstance() {
            return instance;
        }
        
        public void setInstance(Instance instance) {
            this.instance = instance;
        }
        
        public String getClientId() {
            return clientId;
        }
        
        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
        
    }
    
    private class PersistentInstanceSnapshotOperation extends AbstractSnapshotOperation {
        
        private final String snapshotSaveTag = ClassUtils.getSimplaName(getClass()) + ".SAVE";
        
        private final String snapshotLoadTag = ClassUtils.getSimplaName(getClass()) + ".LOAD";
        
        private static final String SNAPSHOT_ARCHIVE = "persistent_instance.zip";
        
        public PersistentInstanceSnapshotOperation(ReentrantReadWriteLock lock) {
            super(lock);
        }
        
        @Override
        protected boolean writeSnapshot(Writer writer) throws IOException {
            final String writePath = writer.getPath();
            final String outputFile = Paths.get(writePath, SNAPSHOT_ARCHIVE).toString();
            final Checksum checksum = new CRC64();
            try (InputStream inputStream = dumpSnapshot()) {
                DiskUtils.compressIntoZipFile("instance", inputStream, outputFile, checksum);
            }
            final LocalFileMeta meta = new LocalFileMeta();
            meta.append(CHECK_SUM_KEY, Long.toHexString(checksum.getValue()));
            return writer.addFile(SNAPSHOT_ARCHIVE, meta);
        }
        
        @Override
        protected boolean readSnapshot(Reader reader) throws Exception {
            final String readerPath = reader.getPath();
            final String sourceFile = Paths.get(readerPath, SNAPSHOT_ARCHIVE).toString();
            final Checksum checksum = new CRC64();
            byte[] snapshotBytes = DiskUtils.decompress(sourceFile, checksum);
            LocalFileMeta fileMeta = reader.getFileMeta(SNAPSHOT_ARCHIVE);
            if (fileMeta.getFileMeta().containsKey(CHECK_SUM_KEY)) {
                if (!Objects.equals(Long.toHexString(checksum.getValue()), fileMeta.get(CHECK_SUM_KEY))) {
                    throw new IllegalArgumentException("Snapshot checksum failed");
                }
            }
            loadSnapshot(snapshotBytes);
            return true;
        }
        
        protected InputStream dumpSnapshot() {
            Map<String, IpPortBasedClient> clientMap = clientManager.showClients();
            ConcurrentHashMap<String, IpPortBasedClient> clone = new ConcurrentHashMap<>(128);
            clientMap.forEach((clientId, client) -> {
                final IpPortBasedClient copy = client.clone();
                copy.clearAllSubscribers();
                clone.put(clientId, client);
            });
            return new ByteArrayInputStream(serializer.serialize(clone));
        }
        
        protected void loadSnapshot(byte[] snapshotBytes) {
            ConcurrentHashMap<String, IpPortBasedClient> newData = serializer.deserialize(snapshotBytes);
            clientManager.loadFromSnapshot(newData);
        }
        
        @Override
        protected String getSnapshotSaveTag() {
            return snapshotSaveTag;
        }
        
        @Override
        protected String getSnapshotLoadTag() {
            return snapshotLoadTag;
        }
    }
    
}
