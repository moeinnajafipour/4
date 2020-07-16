/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.config.remote.response;

import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.remote.response.ResponseCode;

/**
 * ConfigPubishResponse.
 *
 * @author liuzunfei
 * @version $Id: ConfigPubishResponse.java, v 0.1 2020年07月16日 4:59 PM liuzunfei Exp $
 */
public class ConfigPubishResponse extends Response {
    
    public ConfigPubishResponse() {
        super();
    }
    
    public ConfigPubishResponse(int resultCode, String message) {
        super(ConfigResponseTypeConstants.CONFIG_PUBLISH, resultCode, message);
    }
    
    /**
     * Buidl success resposne.
     *
     * @return
     */
    public static ConfigPubishResponse buildSuccessResponse() {
        return new ConfigPubishResponse(ResponseCode.SUCCESS.getCode(), "");
    }
    
    /**
     * Buidl fail resposne.
     *
     * @return
     */
    public static ConfigPubishResponse buildFailResponse(String errorMsg) {
        return new ConfigPubishResponse(ResponseCode.FAIL.getCode(), errorMsg);
    }
}