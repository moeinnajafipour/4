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

package com.alibaba.nacos.client.security;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.http.param.Header;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SecurityProxyTest {
    
    @Test
    public void testLoginSuccess() throws Exception {
        Properties properties = new Properties();
        NacosRestTemplate nacosRestTemplate = mock(NacosRestTemplate.class);
        HttpRestResult<Object> result = new HttpRestResult<>();
        result.setData("{\"accessToken\":\"ttttttttttttttttt\",\"tokenTtl\":1000}");
        result.setCode(200);
        when(nacosRestTemplate.postForm(any(), (Header) any(), any(), any(), any())).thenReturn(result);
        properties.setProperty(PropertyKeyConst.USERNAME, "aaa");
        properties.setProperty(PropertyKeyConst.PASSWORD, "123456");
        SecurityProxy securityProxy = new SecurityProxy(properties, nacosRestTemplate);
        
        boolean ret = securityProxy.login("localhost");
        Assert.assertTrue(ret);
        
    }
    
    @Test
    public void testTestLoginFailCode() throws Exception {
        Properties properties = new Properties();
        NacosRestTemplate nacosRestTemplate = mock(NacosRestTemplate.class);
        HttpRestResult<Object> result = new HttpRestResult<>();
        result.setCode(400);
        when(nacosRestTemplate.postForm(any(), (Header) any(), any(), any(), any())).thenReturn(result);
        
        properties.setProperty(PropertyKeyConst.USERNAME, "aaa");
        properties.setProperty(PropertyKeyConst.PASSWORD, "123456");
        SecurityProxy securityProxy = new SecurityProxy(properties, nacosRestTemplate);
        
        boolean ret = securityProxy.login("localhost");
        Assert.assertFalse(ret);
    }
    
    @Test
    public void testTestLoginFailHttp() throws Exception {
        Properties properties = new Properties();
        NacosRestTemplate nacosRestTemplate = mock(NacosRestTemplate.class);
        when(nacosRestTemplate.postForm(any(), (Header) any(), any(), any(), any())).thenThrow(new Exception());
        properties.setProperty(PropertyKeyConst.USERNAME, "aaa");
        properties.setProperty(PropertyKeyConst.PASSWORD, "123456");
        SecurityProxy securityProxy = new SecurityProxy(properties, nacosRestTemplate);
        
        boolean ret = securityProxy.login("localhost");
        Assert.assertFalse(ret);
    }
    
    @Test
    public void testGetAccessToken() {
    }
    
    @Test
    public void testIsEnabled() {
    }
}
