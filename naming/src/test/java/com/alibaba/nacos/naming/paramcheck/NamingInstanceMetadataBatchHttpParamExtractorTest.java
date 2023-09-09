/*
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
 */

package com.alibaba.nacos.naming.paramcheck;

import static org.junit.Assert.assertEquals;

import com.alibaba.nacos.common.utils.HttpMethod;
import com.alibaba.nacos.core.paramcheck.AbstractHttpParamExtractor;
import com.alibaba.nacos.core.paramcheck.HttpParamExtractorManager;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * The type Naming instance metadata batch http param extractor test.
 *
 * @author zhuoguang
 */
public class NamingInstanceMetadataBatchHttpParamExtractorTest {

    @Test
    public void extractParamAndCheck() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(
                "/nacos"
                        + UtilsAndCommons.NACOS_NAMING_CONTEXT
                        + UtilsAndCommons.NACOS_NAMING_INSTANCE_CONTEXT
                        + "/metadata/batch");
        request.setMethod(HttpMethod.PUT);
        HttpParamExtractorManager manager = HttpParamExtractorManager.getInstance();
        AbstractHttpParamExtractor extractor =
                manager.getExtractor(request.getRequestURI(), request.getMethod(), "naming");
        assertEquals(
                NamingInstanceMetadataBatchHttpParamExtractor.class.getSimpleName(),
                extractor.getClass().getSimpleName());
    }
}
