/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.auth.parser.http;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.api.Resource;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigHttpResourceParserTest {

    @Mock private HttpServletRequest request;

    private ConfigHttpResourceParser resourceParser;

    @Before
    public void setUp() throws Exception {
        resourceParser = new ConfigHttpResourceParser();
    }

    @Test
    @Secured(signType = Constants.Config.CONFIG_MODULE)
    public void testParseWithFullContext() throws NoSuchMethodException {
        Secured secured = getMethodSecure();
        Mockito.when(request.getParameter(eq("tenant"))).thenReturn("testNs");
        Mockito.when(request.getParameter(eq(Constants.GROUP))).thenReturn("testG");
        Mockito.when(request.getParameter(eq(Constants.DATAID))).thenReturn("testD");
        Resource actual = resourceParser.parse(request, secured);
        assertEquals("testNs", actual.getNamespaceId());
        assertEquals("testG", actual.getGroup());
        assertEquals("testD", actual.getName());
        assertEquals(Constants.Config.CONFIG_MODULE, actual.getType());
    }

    @Test
    @Secured(signType = Constants.Config.CONFIG_MODULE)
    public void testParseWithoutNamespace() throws NoSuchMethodException {
        Secured secured = getMethodSecure();
        Mockito.when(request.getParameter(eq(Constants.GROUP))).thenReturn("testG");
        Mockito.when(request.getParameter(eq(Constants.DATAID))).thenReturn("testD");
        Resource actual = resourceParser.parse(request, secured);
        assertEquals(StringUtils.EMPTY, actual.getNamespaceId());
        assertEquals("testG", actual.getGroup());
        assertEquals("testD", actual.getName());
        assertEquals(Constants.Config.CONFIG_MODULE, actual.getType());
    }

    @Test
    @Secured(signType = Constants.Config.CONFIG_MODULE)
    public void testParseWithoutGroup() throws NoSuchMethodException {
        Secured secured = getMethodSecure();
        Mockito.when(request.getParameter(eq("tenant"))).thenReturn("testNs");
        Mockito.when(request.getParameter(eq(Constants.DATAID))).thenReturn("testD");
        Resource actual = resourceParser.parse(request, secured);
        assertEquals("testNs", actual.getNamespaceId());
        assertEquals(StringUtils.EMPTY, actual.getGroup());
        assertEquals("testD", actual.getName());
        assertEquals(Constants.Config.CONFIG_MODULE, actual.getType());
    }

    @Test
    @Secured(signType = Constants.Config.CONFIG_MODULE)
    public void testParseWithoutDataId() throws NoSuchMethodException {
        Secured secured = getMethodSecure();
        Mockito.when(request.getParameter(eq("tenant"))).thenReturn("testNs");
        Mockito.when(request.getParameter(eq(Constants.GROUP))).thenReturn("testG");
        Resource actual = resourceParser.parse(request, secured);
        assertEquals("testNs", actual.getNamespaceId());
        assertEquals("testG", actual.getGroup());
        assertEquals(StringUtils.EMPTY, actual.getName());
        assertEquals(Constants.Config.CONFIG_MODULE, actual.getType());
    }

    private Secured getMethodSecure() throws NoSuchMethodException {
        StackTraceElement[] traces = new Exception().getStackTrace();
        StackTraceElement callerElement = traces[1];
        String methodName = callerElement.getMethodName();
        Method method = this.getClass().getMethod(methodName);
        return method.getAnnotation(Secured.class);
    }
}
