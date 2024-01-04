/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.address.config;

import com.alibaba.nacos.common.utils.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

/**
 * nacos web security configuration.
 *
 * @author onewe
 */
@Configuration
public class AddressServerSecurityConfiguration {
    
    @Bean
    @Order(99)
    public SecurityFilterChain addressServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                        requestMatcherRegistry -> requestMatcherRegistry.mvcMatchers("/nacos/v1/as/**").authenticated()).csrf()
                .disable().httpBasic();
        return http.build();
    }
}
