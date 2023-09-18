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

package com.alibaba.nacos.client.naming.remote.gprc;

import com.alibaba.nacos.api.naming.remote.request.NotifySubscriberRequest;
import com.alibaba.nacos.api.naming.remote.response.NotifySubscriberResponse;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.client.monitor.naming.NamingMetrics;
import com.alibaba.nacos.client.monitor.naming.NamingTrace;
import com.alibaba.nacos.client.naming.cache.ServiceInfoHolder;
import com.alibaba.nacos.common.constant.NacosSemanticAttributes;
import com.alibaba.nacos.common.remote.client.ServerRequestHandler;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;

/**
 * Naming push request handler.
 *
 * @author xiweng.yy
 */
public class NamingPushRequestHandler implements ServerRequestHandler {
    
    private final ServiceInfoHolder serviceInfoHolder;
    
    public NamingPushRequestHandler(ServiceInfoHolder serviceInfoHolder) {
        this.serviceInfoHolder = serviceInfoHolder;
    }
    
    @Override
    public Response requestReply(Request request) {
        if (request instanceof NotifySubscriberRequest) {
            long start = System.currentTimeMillis();
            
            // Trace
            Span span = NamingTrace.getClientNamingWorkerSpan("handleNotifySubscriberRequestFromServer");
            try (Scope ignored = span.makeCurrent()) {
                
                if (span.isRecording()) {
                    span.setAttribute(NacosSemanticAttributes.FUNCTION_CURRENT_NAME,
                            "com.alibaba.nacos.client.naming.remote.gprc.NamingPushRequestHandler.requestReply()");
                    span.setAttribute(NacosSemanticAttributes.RequestAttributes.REQUEST_ID, request.getRequestId());
                }
                
                NotifySubscriberRequest notifyRequest = (NotifySubscriberRequest) request;
                serviceInfoHolder.processServiceInfo(notifyRequest.getServiceInfo());
                
            } catch (Throwable e) {
                span.recordException(e);
                span.setStatus(StatusCode.ERROR, e.getClass().getSimpleName());
                throw e;
            } finally {
                span.end();
            }
            
            // Metrics
            NamingMetrics.incServerRequestHandleCounter();
            NamingMetrics.recordHandleServerRequestCostDurationTimer(NamingPushRequestHandler.class.getSimpleName(),
                    System.currentTimeMillis() - start);
            return new NotifySubscriberResponse();
        }
        return null;
    }
}
