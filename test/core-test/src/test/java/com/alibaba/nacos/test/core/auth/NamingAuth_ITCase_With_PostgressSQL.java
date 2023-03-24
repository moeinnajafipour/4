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
package com.alibaba.nacos.test.core.auth;

import com.alibaba.nacos.Nacos;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author nkorange
 * @since 1.2.0
 */
@SpringBootTest(classes = Nacos.class, properties = {
    "server.servlet.context-path=/nacos",
    "db.num=1",
    "spring.sql.init.platform=postgresql",
    "db.driverClassName[0]=org.postgresql.Driver",
    "db.url[0]=jdbc:postgresql://localhost:5432/nc_config",
    "db.user[0]=nc_config_user",
    "db.password[0]=nc_config_pass",
    "nacos.core.auth.enabled=false"},
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class NamingAuth_ITCase_With_PostgressSQL extends NamingAuth_ITCase {
    @Override
    public String getNacosPassword() {
        return "123456";
    }
}
