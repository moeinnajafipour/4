/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.client.naming;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.api.selector.AbstractSelector;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.monitor.naming.NamingTrace;
import com.alibaba.nacos.client.naming.cache.ServiceInfoHolder;
import com.alibaba.nacos.client.naming.core.Balancer;
import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
import com.alibaba.nacos.client.naming.event.InstancesChangeNotifier;
import com.alibaba.nacos.client.naming.remote.NamingClientProxy;
import com.alibaba.nacos.client.naming.remote.NamingClientProxyDelegate;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.alibaba.nacos.client.naming.utils.InitUtils;
import com.alibaba.nacos.client.naming.utils.UtilAndComs;
import com.alibaba.nacos.client.utils.ValidatorUtils;
import com.alibaba.nacos.common.constant.NacosSemanticAttributes;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.utils.StringUtils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * Nacos Naming Service.
 *
 * @author nkorange
 */
@SuppressWarnings("PMD.ServiceOrDaoClassShouldEndWithImplRule")
public class NacosNamingService implements NamingService {
    
    private static final String DEFAULT_NAMING_LOG_FILE_PATH = "naming.log";
    
    private static final String UP = "UP";
    
    private static final String DOWN = "DOWN";
    
    /**
     * Each Naming service should have different namespace.
     */
    private String namespace;
    
    private String logName;
    
    private ServiceInfoHolder serviceInfoHolder;
    
    private InstancesChangeNotifier changeNotifier;
    
    private NamingClientProxy clientProxy;
    
    private String notifierEventScope;
    
    public NacosNamingService(String serverList) throws NacosException {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, serverList);
        init(properties);
    }
    
    public NacosNamingService(Properties properties) throws NacosException {
        init(properties);
    }
    
    private void init(Properties properties) throws NacosException {
        final NacosClientProperties nacosClientProperties = NacosClientProperties.PROTOTYPE.derive(properties);
        
        ValidatorUtils.checkInitParam(nacosClientProperties);
        this.namespace = InitUtils.initNamespaceForNaming(nacosClientProperties);
        InitUtils.initSerialization();
        InitUtils.initWebRootContext(nacosClientProperties);
        initLogName(nacosClientProperties);
        
        this.notifierEventScope = UUID.randomUUID().toString();
        this.changeNotifier = new InstancesChangeNotifier(this.notifierEventScope);
        NotifyCenter.registerToPublisher(InstancesChangeEvent.class, 16384);
        NotifyCenter.registerSubscriber(changeNotifier);
        this.serviceInfoHolder = new ServiceInfoHolder(namespace, this.notifierEventScope, nacosClientProperties);
        this.clientProxy = new NamingClientProxyDelegate(this.namespace, serviceInfoHolder, nacosClientProperties,
                changeNotifier);
    }
    
    private void initLogName(NacosClientProperties properties) {
        logName = properties.getProperty(UtilAndComs.NACOS_NAMING_LOG_NAME, DEFAULT_NAMING_LOG_FILE_PATH);
    }
    
    @Override
    public void registerInstance(String serviceName, String ip, int port) throws NacosException {
        registerInstance(serviceName, ip, port, Constants.DEFAULT_CLUSTER_NAME);
    }
    
    @Override
    public void registerInstance(String serviceName, String groupName, String ip, int port) throws NacosException {
        registerInstance(serviceName, groupName, ip, port, Constants.DEFAULT_CLUSTER_NAME);
    }
    
    @Override
    public void registerInstance(String serviceName, String ip, int port, String clusterName) throws NacosException {
        registerInstance(serviceName, Constants.DEFAULT_GROUP, ip, port, clusterName);
    }
    
    @Override
    public void registerInstance(String serviceName, String groupName, String ip, int port, String clusterName)
            throws NacosException {
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setWeight(1.0);
        instance.setClusterName(clusterName);
        registerInstance(serviceName, groupName, instance);
    }
    
    @Override
    public void registerInstance(String serviceName, Instance instance) throws NacosException {
        registerInstance(serviceName, Constants.DEFAULT_GROUP, instance);
    }
    
    @Override
    public void registerInstance(String serviceName, String groupName, Instance instance) throws NacosException {
        NamingUtils.checkInstanceIsLegal(instance);
        
        Span span = NamingTrace.getClientNamingServiceSpan("registerInstance");
        try (Scope ignored = span.makeCurrent()) {
            
            if (span.isRecording()) {
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CURRENT_NAME,
                        "com.alibaba.nacos.client.naming.NacosNamingService.registerInstance()");
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CALLED_NAME,
                        "com.alibaba.nacos.client.naming.remote.NamingClientProxy.registerService()");
                span.setAttribute(NacosSemanticAttributes.SERVICE_NAME, serviceName);
                span.setAttribute(NacosSemanticAttributes.GROUP, groupName);
                span.setAttribute(NacosSemanticAttributes.INSTANCE, instance.toString());
                span.setAttribute(NacosSemanticAttributes.NAMESPACE, namespace);
            }
            
            clientProxy.registerService(serviceName, groupName, instance);
            
        } catch (Throwable e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
        
    }
    
    @Override
    public void batchRegisterInstance(String serviceName, String groupName, List<Instance> instances)
            throws NacosException {
        NamingUtils.batchCheckInstanceIsLegal(instances);
        
        Span span = NamingTrace.getClientNamingServiceSpan("batchRegisterInstance");
        try (Scope ignored = span.makeCurrent()) {
            
            if (span.isRecording()) {
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CURRENT_NAME,
                        "com.alibaba.nacos.client.naming.NacosNamingService.batchRegisterInstance()");
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CALLED_NAME,
                        "com.alibaba.nacos.client.naming.remote.NamingClientProxy.batchRegisterService()");
                span.setAttribute(NacosSemanticAttributes.SERVICE_NAME, serviceName);
                span.setAttribute(NacosSemanticAttributes.GROUP, groupName);
                span.setAttribute(NacosSemanticAttributes.INSTANCE, StringUtils.join(instances, ", "));
                span.setAttribute(NacosSemanticAttributes.NAMESPACE, namespace);
            }
            
            clientProxy.batchRegisterService(serviceName, groupName, instances);
            
        } catch (Throwable e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
        
    }
    
    @Override
    public void batchDeregisterInstance(String serviceName, String groupName, List<Instance> instances)
            throws NacosException {
        NamingUtils.batchCheckInstanceIsLegal(instances);
        
        Span span = NamingTrace.getClientNamingServiceSpan("batchDeregisterInstance");
        try (Scope ignored = span.makeCurrent()) {
            
            if (span.isRecording()) {
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CURRENT_NAME,
                        "com.alibaba.nacos.client.naming.NacosNamingService.batchDeregisterInstance()");
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CALLED_NAME,
                        "com.alibaba.nacos.client.naming.remote.NamingClientProxy.batchDeregisterService()");
                span.setAttribute(NacosSemanticAttributes.SERVICE_NAME, serviceName);
                span.setAttribute(NacosSemanticAttributes.GROUP, groupName);
                span.setAttribute(NacosSemanticAttributes.INSTANCE, StringUtils.join(instances, ", "));
                span.setAttribute(NacosSemanticAttributes.NAMESPACE, namespace);
            }
            
            clientProxy.batchDeregisterService(serviceName, groupName, instances);
            
        } catch (Throwable e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
        
    }
    
    @Override
    public void deregisterInstance(String serviceName, String ip, int port) throws NacosException {
        deregisterInstance(serviceName, ip, port, Constants.DEFAULT_CLUSTER_NAME);
    }
    
    @Override
    public void deregisterInstance(String serviceName, String groupName, String ip, int port) throws NacosException {
        deregisterInstance(serviceName, groupName, ip, port, Constants.DEFAULT_CLUSTER_NAME);
    }
    
    @Override
    public void deregisterInstance(String serviceName, String ip, int port, String clusterName) throws NacosException {
        deregisterInstance(serviceName, Constants.DEFAULT_GROUP, ip, port, clusterName);
    }
    
    @Override
    public void deregisterInstance(String serviceName, String groupName, String ip, int port, String clusterName)
            throws NacosException {
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setClusterName(clusterName);
        deregisterInstance(serviceName, groupName, instance);
    }
    
    @Override
    public void deregisterInstance(String serviceName, Instance instance) throws NacosException {
        deregisterInstance(serviceName, Constants.DEFAULT_GROUP, instance);
    }
    
    @Override
    public void deregisterInstance(String serviceName, String groupName, Instance instance) throws NacosException {
        
        Span span = NamingTrace.getClientNamingServiceSpan("deregisterInstance");
        try (Scope ignored = span.makeCurrent()) {
            
            if (span.isRecording()) {
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CURRENT_NAME,
                        "com.alibaba.nacos.client.naming.NacosNamingService.deregisterInstance()");
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CALLED_NAME,
                        "com.alibaba.nacos.client.naming.remote.NamingClientProxy.deregisterService()");
                span.setAttribute(NacosSemanticAttributes.SERVICE_NAME, serviceName);
                span.setAttribute(NacosSemanticAttributes.GROUP, groupName);
                span.setAttribute(NacosSemanticAttributes.INSTANCE, instance.toString());
                span.setAttribute(NacosSemanticAttributes.NAMESPACE, namespace);
            }
            
            clientProxy.deregisterService(serviceName, groupName, instance);
            
        } catch (Throwable e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
        
    }
    
    @Override
    public List<Instance> getAllInstances(String serviceName) throws NacosException {
        return getAllInstances(serviceName, new ArrayList<>());
    }
    
    @Override
    public List<Instance> getAllInstances(String serviceName, String groupName) throws NacosException {
        return getAllInstances(serviceName, groupName, new ArrayList<>());
    }
    
    @Override
    public List<Instance> getAllInstances(String serviceName, boolean subscribe) throws NacosException {
        return getAllInstances(serviceName, new ArrayList<>(), subscribe);
    }
    
    @Override
    public List<Instance> getAllInstances(String serviceName, String groupName, boolean subscribe)
            throws NacosException {
        return getAllInstances(serviceName, groupName, new ArrayList<>(), subscribe);
    }
    
    @Override
    public List<Instance> getAllInstances(String serviceName, List<String> clusters) throws NacosException {
        return getAllInstances(serviceName, clusters, true);
    }
    
    @Override
    public List<Instance> getAllInstances(String serviceName, String groupName, List<String> clusters)
            throws NacosException {
        return getAllInstances(serviceName, groupName, clusters, true);
    }
    
    @Override
    public List<Instance> getAllInstances(String serviceName, List<String> clusters, boolean subscribe)
            throws NacosException {
        return getAllInstances(serviceName, Constants.DEFAULT_GROUP, clusters, subscribe);
    }
    
    @Override
    public List<Instance> getAllInstances(String serviceName, String groupName, List<String> clusters,
            boolean subscribe) throws NacosException {
        ServiceInfo serviceInfo;
        String clusterString = StringUtils.join(clusters, ",");
        
        Span span = NamingTrace.getClientNamingServiceSpan("getAllInstances");
        try (Scope ignored = span.makeCurrent()) {
            
            if (span.isRecording()) {
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CURRENT_NAME,
                        "com.alibaba.nacos.client.naming.NacosNamingService.getAllInstances()");
                span.setAttribute(NacosSemanticAttributes.SERVICE_NAME, serviceName);
                span.setAttribute(NacosSemanticAttributes.GROUP, groupName);
                span.setAttribute(NacosSemanticAttributes.CLUSTER, clusterString);
                span.setAttribute(NacosSemanticAttributes.SUBSCRIBE, subscribe);
                span.setAttribute(NacosSemanticAttributes.NAMESPACE, namespace);
            }
            
            if (subscribe) {
                serviceInfo = serviceInfoHolder.getServiceInfo(serviceName, groupName, clusterString);
                if (null == serviceInfo || !clientProxy.isSubscribed(serviceName, groupName, clusterString)) {
                    serviceInfo = clientProxy.subscribe(serviceName, groupName, clusterString);
                }
            } else {
                serviceInfo = clientProxy.queryInstancesOfService(serviceName, groupName, clusterString, 0, false);
            }
            
        } catch (Throwable e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
        
        List<Instance> list;
        if (serviceInfo == null || CollectionUtils.isEmpty(list = serviceInfo.getHosts())) {
            return new ArrayList<>();
        }
        return list;
    }
    
    @Override
    public List<Instance> selectInstances(String serviceName, boolean healthy) throws NacosException {
        return selectInstances(serviceName, new ArrayList<>(), healthy);
    }
    
    @Override
    public List<Instance> selectInstances(String serviceName, String groupName, boolean healthy) throws NacosException {
        return selectInstances(serviceName, groupName, healthy, true);
    }
    
    @Override
    public List<Instance> selectInstances(String serviceName, boolean healthy, boolean subscribe)
            throws NacosException {
        return selectInstances(serviceName, new ArrayList<>(), healthy, subscribe);
    }
    
    @Override
    public List<Instance> selectInstances(String serviceName, String groupName, boolean healthy, boolean subscribe)
            throws NacosException {
        return selectInstances(serviceName, groupName, new ArrayList<>(), healthy, subscribe);
    }
    
    @Override
    public List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy)
            throws NacosException {
        return selectInstances(serviceName, clusters, healthy, true);
    }
    
    @Override
    public List<Instance> selectInstances(String serviceName, String groupName, List<String> clusters, boolean healthy)
            throws NacosException {
        return selectInstances(serviceName, groupName, clusters, healthy, true);
    }
    
    @Override
    public List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy, boolean subscribe)
            throws NacosException {
        return selectInstances(serviceName, Constants.DEFAULT_GROUP, clusters, healthy, subscribe);
    }
    
    @Override
    public List<Instance> selectInstances(String serviceName, String groupName, List<String> clusters, boolean healthy,
            boolean subscribe) throws NacosException {
        
        ServiceInfo serviceInfo;
        String clusterString = StringUtils.join(clusters, ",");
        
        Span span = NamingTrace.getClientNamingServiceSpan("selectInstances");
        try (Scope ignored = span.makeCurrent()) {
            
            if (span.isRecording()) {
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CURRENT_NAME,
                        "com.alibaba.nacos.client.naming.NacosNamingService.selectInstances()");
                span.setAttribute(NacosSemanticAttributes.SERVICE_NAME, serviceName);
                span.setAttribute(NacosSemanticAttributes.GROUP, groupName);
                span.setAttribute(NacosSemanticAttributes.CLUSTER, clusterString);
                span.setAttribute(NacosSemanticAttributes.SUBSCRIBE, subscribe);
                span.setAttribute(NacosSemanticAttributes.NAMESPACE, namespace);
            }
            
            if (subscribe) {
                serviceInfo = serviceInfoHolder.getServiceInfo(serviceName, groupName, clusterString);
                if (null == serviceInfo) {
                    serviceInfo = clientProxy.subscribe(serviceName, groupName, clusterString);
                }
            } else {
                serviceInfo = clientProxy.queryInstancesOfService(serviceName, groupName, clusterString, 0, false);
            }
            
        } catch (Throwable e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
        
        return selectInstances(serviceInfo, healthy);
    }
    
    private List<Instance> selectInstances(ServiceInfo serviceInfo, boolean healthy) {
        List<Instance> list;
        if (serviceInfo == null || CollectionUtils.isEmpty(list = serviceInfo.getHosts())) {
            return new ArrayList<>();
        }
        
        Iterator<Instance> iterator = list.iterator();
        while (iterator.hasNext()) {
            Instance instance = iterator.next();
            if (healthy != instance.isHealthy() || !instance.isEnabled() || instance.getWeight() <= 0) {
                iterator.remove();
            }
        }
        
        return list;
    }
    
    @Override
    public Instance selectOneHealthyInstance(String serviceName) throws NacosException {
        return selectOneHealthyInstance(serviceName, new ArrayList<>());
    }
    
    @Override
    public Instance selectOneHealthyInstance(String serviceName, String groupName) throws NacosException {
        return selectOneHealthyInstance(serviceName, groupName, true);
    }
    
    @Override
    public Instance selectOneHealthyInstance(String serviceName, boolean subscribe) throws NacosException {
        return selectOneHealthyInstance(serviceName, new ArrayList<>(), subscribe);
    }
    
    @Override
    public Instance selectOneHealthyInstance(String serviceName, String groupName, boolean subscribe)
            throws NacosException {
        return selectOneHealthyInstance(serviceName, groupName, new ArrayList<>(), subscribe);
    }
    
    @Override
    public Instance selectOneHealthyInstance(String serviceName, List<String> clusters) throws NacosException {
        return selectOneHealthyInstance(serviceName, clusters, true);
    }
    
    @Override
    public Instance selectOneHealthyInstance(String serviceName, String groupName, List<String> clusters)
            throws NacosException {
        return selectOneHealthyInstance(serviceName, groupName, clusters, true);
    }
    
    @Override
    public Instance selectOneHealthyInstance(String serviceName, List<String> clusters, boolean subscribe)
            throws NacosException {
        return selectOneHealthyInstance(serviceName, Constants.DEFAULT_GROUP, clusters, subscribe);
    }
    
    @Override
    public Instance selectOneHealthyInstance(String serviceName, String groupName, List<String> clusters,
            boolean subscribe) throws NacosException {
        ServiceInfo serviceInfo;
        String clusterString = StringUtils.join(clusters, ",");
        
        Span span = NamingTrace.getClientNamingServiceSpan("selectOneHealthyInstance");
        try (Scope ignored = span.makeCurrent()) {
            
            if (span.isRecording()) {
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CURRENT_NAME,
                        "com.alibaba.nacos.client.naming.NacosNamingService.selectOneHealthyInstance()");
                span.setAttribute(NacosSemanticAttributes.SERVICE_NAME, serviceName);
                span.setAttribute(NacosSemanticAttributes.GROUP, groupName);
                span.setAttribute(NacosSemanticAttributes.CLUSTER, clusterString);
                span.setAttribute(NacosSemanticAttributes.SUBSCRIBE, subscribe);
                span.setAttribute(NacosSemanticAttributes.NAMESPACE, namespace);
            }
            
            if (subscribe) {
                serviceInfo = serviceInfoHolder.getServiceInfo(serviceName, groupName, clusterString);
                if (null == serviceInfo) {
                    serviceInfo = clientProxy.subscribe(serviceName, groupName, clusterString);
                }
            } else {
                serviceInfo = clientProxy.queryInstancesOfService(serviceName, groupName, clusterString, 0, false);
            }
            
        } catch (Throwable e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
        
        return Balancer.RandomByWeight.selectHost(serviceInfo);
    }
    
    @Override
    public void subscribe(String serviceName, EventListener listener) throws NacosException {
        subscribe(serviceName, new ArrayList<>(), listener);
    }
    
    @Override
    public void subscribe(String serviceName, String groupName, EventListener listener) throws NacosException {
        subscribe(serviceName, groupName, new ArrayList<>(), listener);
    }
    
    @Override
    public void subscribe(String serviceName, List<String> clusters, EventListener listener) throws NacosException {
        subscribe(serviceName, Constants.DEFAULT_GROUP, clusters, listener);
    }
    
    @Override
    public void subscribe(String serviceName, String groupName, List<String> clusters, EventListener listener)
            throws NacosException {
        if (null == listener) {
            return;
        }
        String clusterString = StringUtils.join(clusters, ",");
        
        Span span = NamingTrace.getClientNamingServiceSpan("subscribe");
        try (Scope ignored = span.makeCurrent()) {
            
            if (span.isRecording()) {
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CURRENT_NAME,
                        "com.alibaba.nacos.client.naming.NacosNamingService.subscribe()");
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CALLED_NAME,
                        "com.alibaba.nacos.client.naming.remote.NamingClientProxy.subscribe()");
                span.setAttribute(NacosSemanticAttributes.SERVICE_NAME, serviceName);
                span.setAttribute(NacosSemanticAttributes.GROUP, groupName);
                span.setAttribute(NacosSemanticAttributes.CLUSTER, clusterString);
                span.setAttribute(NacosSemanticAttributes.NAMESPACE, namespace);
            }
            
            changeNotifier.registerListener(groupName, serviceName, clusterString, listener);
            clientProxy.subscribe(serviceName, groupName, clusterString);
            
        } catch (Throwable e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
        
    }
    
    @Override
    public void unsubscribe(String serviceName, EventListener listener) throws NacosException {
        unsubscribe(serviceName, new ArrayList<>(), listener);
    }
    
    @Override
    public void unsubscribe(String serviceName, String groupName, EventListener listener) throws NacosException {
        unsubscribe(serviceName, groupName, new ArrayList<>(), listener);
    }
    
    @Override
    public void unsubscribe(String serviceName, List<String> clusters, EventListener listener) throws NacosException {
        unsubscribe(serviceName, Constants.DEFAULT_GROUP, clusters, listener);
    }
    
    @Override
    public void unsubscribe(String serviceName, String groupName, List<String> clusters, EventListener listener)
            throws NacosException {
        String clustersString = StringUtils.join(clusters, ",");
        
        Span span = NamingTrace.getClientNamingServiceSpan("unsubscribe");
        try (Scope ignored = span.makeCurrent()) {
            
            if (span.isRecording()) {
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CURRENT_NAME,
                        "com.alibaba.nacos.client.naming.NacosNamingService.unsubscribe()");
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CALLED_NAME,
                        "com.alibaba.nacos.client.naming.remote.NamingClientProxy.unsubscribe()");
                span.setAttribute(NacosSemanticAttributes.SERVICE_NAME, serviceName);
                span.setAttribute(NacosSemanticAttributes.GROUP, groupName);
                span.setAttribute(NacosSemanticAttributes.CLUSTER, clustersString);
                span.setAttribute(NacosSemanticAttributes.NAMESPACE, namespace);
            }
            
            changeNotifier.deregisterListener(groupName, serviceName, clustersString, listener);
            if (!changeNotifier.isSubscribed(groupName, serviceName, clustersString)) {
                clientProxy.unsubscribe(serviceName, groupName, clustersString);
            }
            
        } catch (Throwable e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
        
    }
    
    @Override
    public ListView<String> getServicesOfServer(int pageNo, int pageSize) throws NacosException {
        return getServicesOfServer(pageNo, pageSize, Constants.DEFAULT_GROUP);
    }
    
    @Override
    public ListView<String> getServicesOfServer(int pageNo, int pageSize, String groupName) throws NacosException {
        return getServicesOfServer(pageNo, pageSize, groupName, null);
    }
    
    @Override
    public ListView<String> getServicesOfServer(int pageNo, int pageSize, AbstractSelector selector)
            throws NacosException {
        return getServicesOfServer(pageNo, pageSize, Constants.DEFAULT_GROUP, selector);
    }
    
    @Override
    public ListView<String> getServicesOfServer(int pageNo, int pageSize, String groupName, AbstractSelector selector)
            throws NacosException {
        ListView<String> result;
        
        Span span = NamingTrace.getClientNamingServiceSpan("getServicesOfServer");
        try (Scope ignored = span.makeCurrent()) {
            
            if (span.isRecording()) {
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CURRENT_NAME,
                        "com.alibaba.nacos.client.naming.NacosNamingService.getServicesOfServer()");
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CALLED_NAME,
                        "com.alibaba.nacos.client.naming.remote.NamingClientProxy.getServiceList()");
                span.setAttribute(NacosSemanticAttributes.PAGE_NO, pageNo);
                span.setAttribute(NacosSemanticAttributes.PAGE_SIZE, pageSize);
                span.setAttribute(NacosSemanticAttributes.GROUP, groupName);
                span.setAttribute(NacosSemanticAttributes.NAMESPACE, namespace);
            }
            
            result = clientProxy.getServiceList(pageNo, pageSize, groupName, selector);
            
        } catch (Throwable e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
        
        return result;
    }
    
    @Override
    public List<ServiceInfo> getSubscribeServices() {
        return changeNotifier.getSubscribeServices();
    }
    
    @Override
    public String getServerStatus() {
        boolean result;
        
        Span span = NamingTrace.getClientNamingServiceSpan("getServerStatus");
        try (Scope ignored = span.makeCurrent()) {
            
            if (span.isRecording()) {
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CURRENT_NAME,
                        "com.alibaba.nacos.client.naming.NacosNamingService.getServerStatus()");
                span.setAttribute(NacosSemanticAttributes.FUNCTION_CALLED_NAME,
                        "com.alibaba.nacos.client.naming.remote.NamingClientProxy.serverHealthy()");
                span.setAttribute(NacosSemanticAttributes.NAMESPACE, namespace);
            }
            
            result = clientProxy.serverHealthy();
            
            if (result) {
                span.setStatus(StatusCode.OK, "Server is up");
            } else {
                span.setStatus(StatusCode.ERROR, "Server is down");
            }
            
        } catch (Throwable e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
        
        return result ? UP : DOWN;
    }
    
    @Override
    public void shutDown() throws NacosException {
        serviceInfoHolder.shutdown();
        clientProxy.shutdown();
        NotifyCenter.deregisterSubscriber(changeNotifier);
        
    }
}
