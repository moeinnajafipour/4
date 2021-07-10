package com.alibaba.nacos.api.remote;

import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.response.Response;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * PayLoaderProviderScanner scan all request and response.
 *
 * @author dingjuntao
 * @date 2021/7/8 16:44
 */
public class PayLoaderProviderScanner  {

    private HashSet<PayLoaderProvider> payLoaderProviderSet = new HashSet<>();
    
    /**
     * PayLoaderProviderScanner init, load payLoaderProviders.
     */
    public void init() {
        ServiceLoader<PayLoaderProvider> payLoaderProviders = ServiceLoader.load(PayLoaderProvider.class);
        
        for (PayLoaderProvider each : payLoaderProviders) {
            boolean addFlag = payLoaderProviderSet.add(each);
            if (!addFlag) {
                throw new RuntimeException(String.format("Fail to Load Service, clazz:%s ", each.getClass().getCanonicalName()));
            }
        }
    }

    public Set<Class<? extends Request>> getAllPayLoadRequestSet() {
        Set<Class<? extends Request>> allPayLoadRequestSet = new HashSet<>();
        for (PayLoaderProvider eachPayLoaderProvider : payLoaderProviderSet) {
            Set<Class<? extends Request>> newPayLoadRequestSet = eachPayLoaderProvider.getPayLoadRequestSet();
            for (Class<? extends Request> newPayLoadRequest : newPayLoadRequestSet) {
                boolean addFlag = allPayLoadRequestSet.add(newPayLoadRequest);
                if (!addFlag) {
                    throw new RuntimeException(String.format("Fail to Load Request class, clazz:%s ", newPayLoadRequest.getCanonicalName()));
                }
            }
        }
        return allPayLoadRequestSet;
    }
    
    public Set<Class<? extends Response>> getAllPayLoadResponseSet() {
        Set<Class<? extends Response>> allPayLoadResponseSet = new HashSet<>();
        for (PayLoaderProvider eachPayLoaderProvider : payLoaderProviderSet) {
            Set<Class<? extends Response>> newPayLoadResponseSet = eachPayLoaderProvider.getPayLoadResponseSet();
            for (Class<? extends Response> newPayLoadResponse : newPayLoadResponseSet) {
                boolean addFlag = allPayLoadResponseSet.add(newPayLoadResponse);
                if (!addFlag) {
                    throw new RuntimeException(String.format("Fail to Load Response class, clazz:%s ", newPayLoadResponse.getCanonicalName()));
                }
            }
        }
        return allPayLoadResponseSet;
    }
    
}
