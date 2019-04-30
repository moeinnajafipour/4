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

package com.alibaba.nacos.api.naming;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.selector.AbstractSelector;

import java.util.Map;

/**
 * Operations related to Nacos
 *
 * @author liaochuntao
 * @since 1.0.0
 */
public interface MaintainService {

    /**
     * query service
     *
     * @param serviceName
     * @return
     * @throws NacosException
     */
    Service selectOneService(String serviceName) throws NacosException;

    /**
     * query service
     *
     * @param serviceName
     * @param groupName
     * @return
     * @throws NacosException
     */
    Service selectOneService(String serviceName, String groupName) throws NacosException;

    /**
     * create service to Nacos
     *
     * @param serviceName name of service
     * @throws NacosException
     */
    void createService(String serviceName) throws NacosException;

    /**
     * create service to Nacos
     *
     * @param serviceName name of service
     * @param groupName   group of service
     * @throws NacosException
     */
    void createService(String serviceName, String groupName) throws NacosException;

    /**
     * create service to Nacos
     *
     * @param serviceName           name of service
     * @param groupName             group of service
     * @param protectThreshold      protectThreshold of service
     * @throws NacosException
     */
    void createService(String serviceName, String groupName, Float protectThreshold) throws NacosException;

    /**
     * create service to Nacos
     *
     * @param serviceName       name of service
     * @param groupName         group of service
     * @param protectThreshold  protectThreshold of service
     * @param expression        expression of selector
     * @throws NacosException
     */
    void createService(String serviceName, String groupName, Float protectThreshold, String expression) throws NacosException;

    /**
     * create service to Nacos
     *
     * @param service   name of service
     * @param selector  selector
     * @throws NacosException
     */
    void createService(Service service, AbstractSelector selector) throws NacosException;

    /**
     * delete service from Nacos
     *
     * @param serviceName name of service
     * @return if delete service success return true
     * @throws NacosException
     */
    boolean deleteService(String serviceName) throws NacosException;

    /**
     * delete service from Nacos
     *
     * @param serviceName name of service
     * @param groupName   group of service
     * @return if delete service success return true
     * @throws NacosException
     */
    boolean deleteService(String serviceName, String groupName) throws NacosException;

    /**
     * update service to Nacos
     *
     * @param serviceName       name of service
     * @param groupName         group of service
     * @param protectThreshold  protectThreshold of service
     * @throws NacosException
     */
    void updateService(String serviceName, String groupName, Float protectThreshold) throws NacosException;

    /**
     * update service to Nacos
     *
     * @param serviceName       name of service
     * @param groupName         group of service
     * @param protectThreshold  protectThreshold of service
     * @param metadata          metadata of service
     * @throws NacosException
     */
    void updateService(String serviceName, String groupName, Float protectThreshold, Map<String, String> metadata) throws NacosException;

    /**
     * update service to Nacos with selector
     *
     * @param service    {@link Service} pojo of service
     * @param selector   {@link AbstractSelector} pojo of selector
     * @throws NacosException
     */
    void updateService(Service service, AbstractSelector selector) throws NacosException;

}
