/*
 *
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.client.monitor.config;

import com.alibaba.nacos.client.monitor.TraceMonitor;
import com.alibaba.nacos.common.utils.VersionUtils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;

/**
 * Config traces management.
 *
 * @author <a href="https://github.com/FAWC438">FAWC438</a>
 */
public class ConfigTrace {
    
    private static final String NACOS_CLIENT_CONFIG_BASE_SPAN = "Nacos.client.config";
    
    private static final String NACOS_CLIENT_CONFIG_RPC_SPAN = NACOS_CLIENT_CONFIG_BASE_SPAN + ".rpc";
    
    private static final String NACOS_CLIENT_CONFIG_HTTP_SPAN = NACOS_CLIENT_CONFIG_BASE_SPAN + ".http";
    
    private static final String NACOS_CLIENT_CONFIG_SERVICE_SPAN = NACOS_CLIENT_CONFIG_BASE_SPAN + ".service";
    
    private static final String NACOS_CLIENT_CONFIG_WORKER_SPAN = NACOS_CLIENT_CONFIG_BASE_SPAN + ".worker";
    
    private static final String NACOS_CLIENT_VERSION_ATTRIBUTE = "nacos.client.version";
    
    public static Span getClientConfigRpcSpan(String rpcType) {
        String spanName = NACOS_CLIENT_CONFIG_RPC_SPAN + "/" + rpcType.toUpperCase();
        return TraceMonitor.getTracer().spanBuilder(spanName).setSpanKind(SpanKind.CLIENT)
                .setAttribute(NACOS_CLIENT_VERSION_ATTRIBUTE, VersionUtils.getFullClientVersion()).startSpan();
    }
    
    /**
     * Get the Nacos client config http span.
     *
     * @param method the http method
     * @return the OpenTelemetry span
     */
    public static Span getClientConfigHttpSpan(String method) {
        String spanName = NACOS_CLIENT_CONFIG_HTTP_SPAN + "/" + method.toUpperCase();
        return TraceMonitor.getTracer().spanBuilder(spanName).setSpanKind(SpanKind.CLIENT)
                .setAttribute(NACOS_CLIENT_VERSION_ATTRIBUTE, VersionUtils.getFullClientVersion()).startSpan();
    }
    
    public static Span getClientConfigServiceSpan(String spanNameExtension) {
        String spanName = NACOS_CLIENT_CONFIG_SERVICE_SPAN + "/" + spanNameExtension;
        return TraceMonitor.getTracer().spanBuilder(spanName).setSpanKind(SpanKind.CLIENT)
                .setAttribute(NACOS_CLIENT_VERSION_ATTRIBUTE, VersionUtils.getFullClientVersion()).startSpan();
    }
    
    public static Span getClientConfigWorkerSpan(String spanNameExtension) {
        String spanName = NACOS_CLIENT_CONFIG_WORKER_SPAN + "/" + spanNameExtension;
        return TraceMonitor.getTracer().spanBuilder(spanName).setSpanKind(SpanKind.CLIENT)
                .setAttribute(NACOS_CLIENT_VERSION_ATTRIBUTE, VersionUtils.getFullClientVersion()).startSpan();
    }
    
}
