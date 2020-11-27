package com.alibaba.nacos.naming.core.v2.service;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.naming.consistency.persistent.impl.PersistentServiceProcessor;
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.client.Client;
import com.alibaba.nacos.naming.core.v2.client.manager.ClientManager;
import com.alibaba.nacos.naming.core.v2.client.manager.ClientManagerDelegate;
import com.alibaba.nacos.naming.core.v2.event.client.ClientOperationEvent;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.core.v2.service.impl.EphemeralClientOperationServiceImpl;
import com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.pojo.Subscriber;
import org.springframework.stereotype.Component;

/**
 * Implementation of external exposure.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@Component
public class ClientOperationServiceProxy implements ClientOperationService {
    
    private final ClientOperationService ephemeraClientOperationService;
    
    private final ClientOperationService persistentClientOperationService;
    
    private final ClientManager clientManager;
    
    public ClientOperationServiceProxy(final PersistentServiceProcessor processor, ClientManagerDelegate clientManager) {
        this.clientManager = clientManager;
        this.ephemeraClientOperationService = new EphemeralClientOperationServiceImpl(clientManager);
        this.persistentClientOperationService = new PersistentClientOperationServiceImpl(processor, clientManager);
    }
    
    @Override
    public void registerInstance(Service service, Instance instance, String clientId) {
        final ClientOperationService operationService = switchByClientID(instance);
        operationService.registerInstance(service, instance, clientId);
    }
    
    @Override
    public void deregisterInstance(Service service, Instance instance, String clientId) {
        if (!ServiceManager.getInstance().containSingleton(service)) {
            Loggers.SRV_LOG.warn("remove instance from non-exist service: {}", service);
            return;
        }
        final ClientOperationService operationService = switchByClientID(instance);
        operationService.deregisterInstance(service, instance, clientId);
    }
    
    @Override
    public void subscribeService(Service service, Subscriber subscriber, String clientId) {
        Service singleton = ServiceManager.getInstance().getSingleton(service);
        Client client = clientManager.getClient(clientId);
        client.addServiceSubscriber(singleton, subscriber);
        client.setLastUpdatedTime();
        NotifyCenter.publishEvent(new ClientOperationEvent.ClientSubscribeServiceEvent(singleton, clientId));
    }
    
    @Override
    public void unsubscribeService(Service service, Subscriber subscriber, String clientId) {
        Service singleton = ServiceManager.getInstance().getSingleton(service);
        Client client = clientManager.getClient(clientId);
        client.removeServiceSubscriber(singleton);
        client.setLastUpdatedTime();
        NotifyCenter.publishEvent(new ClientOperationEvent.ClientUnsubscribeServiceEvent(singleton, clientId));
    }
    
    private ClientOperationService switchByClientID(final Instance instance) {
        return instance.isEphemeral() ? ephemeraClientOperationService : persistentClientOperationService;
    }
}
