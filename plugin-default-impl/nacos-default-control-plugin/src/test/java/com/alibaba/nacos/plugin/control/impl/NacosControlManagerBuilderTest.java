/*
 *
 *  * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.alibaba.nacos.plugin.control.impl;

import com.alibaba.nacos.plugin.control.connection.ConnectionControlManager;
import com.alibaba.nacos.plugin.control.tps.TpsControlManager;
import org.junit.Assert;
import org.junit.Test;

/**
 * NacosControlManagerBuilderTest.
 *
 * @author : huangtianhui
 */
public class NacosControlManagerBuilderTest {
    
    @Test
    public void test() {
        NacosControlManagerBuilder nacosControlManagerBuilder = new NacosControlManagerBuilder();
        ConnectionControlManager connectionControlManager = nacosControlManagerBuilder.buildConnectionControlManager();
        TpsControlManager tpsControlManager = nacosControlManagerBuilder.buildTpsControlManager();
        
        Assert.assertEquals("nacos", tpsControlManager.getName());
        Assert.assertEquals("nacos", connectionControlManager.getName());
        Assert.assertEquals("nacos", nacosControlManagerBuilder.getName());
    }
    
}
