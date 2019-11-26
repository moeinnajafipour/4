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
package com.alibaba.nacos.naming.core;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.nacos.naming.healthcheck.HealthCheckReactor;
import com.alibaba.nacos.naming.healthcheck.HealthCheckStatus;
import com.alibaba.nacos.naming.healthcheck.HealthCheckTask;
import com.alibaba.nacos.naming.misc.Loggers;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author nkorange
 * @author jifengnan 2019-04-26
 */
public class Cluster extends com.alibaba.nacos.api.naming.pojo.Cluster implements Cloneable {

    private static final String CLUSTER_NAME_SYNTAX = "[0-9a-zA-Z-]+";
    /**
     * a addition for same site routing, can group multiple sites into a region, like Hangzhou, Shanghai, etc.
     */
    private String sitegroup = StringUtils.EMPTY;

    private int defCkport = 80;

    private int defIPPort = -1;

    @JSONField(serialize = false)
    private HealthCheckTask checkTask;

    @JSONField(serialize = false)
    private Set<Instance> persistentInstances = new HashSet<>();

    @JSONField(serialize = false)
    private Set<Instance> ephemeralInstances = new HashSet<>();

    @JSONField(serialize = false)
    private Service service;

    @JSONField(serialize = false)
    private volatile boolean inited = false;

    private Map<String, String> metadata = new ConcurrentHashMap<>();

    public Cluster() {
    }

    /**
     * Create a cluster.
     * <p>the cluster name cannot be null, and only the arabic numerals, letters and endashes are allowed.
     *
     * @param clusterName the cluster name
     * @param service     the service to which the current cluster belongs
     * @throws IllegalArgumentException the service is null, or the cluster name is null, or the cluster name is illegal
     * @author jifengnan 2019-04-26
     * @since 1.0.1
     */
    public Cluster(String clusterName, Service service) {
        this.setName(clusterName);
        this.service = service;
        validate();
    }

    public int getDefIPPort() {
        // for compatibility with old entries
        return defIPPort == -1 ? defCkport : defIPPort;
    }

    public void setDefIPPort(int defIPPort) {
        if (defIPPort == 0) {
            throw new IllegalArgumentException("defIPPort can not be 0");
        }
        this.defIPPort = defIPPort;
    }

    /**
     * 获取所有节点
     * @return
     */
    public List<Instance> allIPs() {
        List<Instance> allInstances = new ArrayList<>();
        /**
         * 持久化节点
         */
        allInstances.addAll(persistentInstances);
        /**
         * 临时节点
         */
        allInstances.addAll(ephemeralInstances);
        return allInstances;
    }

    /**
     * 是否获取nacos上的临时节点
     * @param ephemeral true:临时节点   false:持久化节点
     * @return
     */
    public List<Instance> allIPs(boolean ephemeral) {
        return ephemeral ? new ArrayList<>(ephemeralInstances) : new ArrayList<>(persistentInstances);
    }

    /**
     * 初始化
     */
    public void init() {
        if (inited) {
            return;
        }
        /**
         * 默认tcp
         */
        checkTask = new HealthCheckTask(this);

        /**
         * 执行HealthCheckTask
         */
        HealthCheckReactor.scheduleCheck(checkTask);
        inited = true;
    }

    public void destroy() {
        if (checkTask != null) {
            checkTask.setCancelled(true);
        }
    }

    public HealthCheckTask getHealthCheckTask() {
        return checkTask;
    }

    public Service getService() {
        return service;
    }

    /**
     * Replace the service for the current cluster.
     * <p>  the service shouldn't be replaced. so if the service is not empty will nothing to do.
     * (the service fields can be changed, but the service A shouldn't be replaced to service B).
     * If the service of a cluster is required to replace, actually, a new cluster is required.
     *
     * @param service the new service
     */
    public void setService(Service service) {
        if (this.service != null) {
            return;
        }
        this.service = service;
    }

    /**
     * this method has been deprecated, the service name shouldn't be changed.
     *
     * @param serviceName the service name
     * @author jifengnan  2019-04-26
     * @since 1.0.1
     */
    @Deprecated
    @Override
    public void setServiceName(String serviceName) {
        super.setServiceName(serviceName);
    }

    /**
     * Get the service name of the current cluster.
     * <p>Note that the returned service name is not the name which set by {@link #setServiceName(String)},
     * but the name of the service to which the current cluster belongs.
     *
     * @return the service name of the current cluster.
     */
    @Override
    public String getServiceName() {
        if (service != null) {
            return service.getName();
        } else {
            return super.getServiceName();
        }
    }

    @Override
    public Cluster clone() throws CloneNotSupportedException {
        super.clone();
        Cluster cluster = new Cluster(this.getName(), service);
        cluster.setHealthChecker(getHealthChecker().clone());
        cluster.persistentInstances = new HashSet<>();
        cluster.checkTask = null;
        cluster.metadata = new HashMap<>(metadata);
        return cluster;
    }

    /**
     * 更新cluster对应的Instance集合
     * @param ips
     * @param ephemeral
     */
    public void updateIPs(List<Instance> ips, boolean ephemeral) {

        /**
         * 获取当前cluster对应的Instance集合
         */
        Set<Instance> toUpdateInstances = ephemeral ? ephemeralInstances : persistentInstances;

        HashMap<String, Instance> oldIPMap = new HashMap<>(toUpdateInstances.size());

        for (Instance ip : toUpdateInstances) {
            oldIPMap.put(ip.getDatumKey(), ip);
        }

        /**
         * 仅在ips中存在  获取在两者中相同ip:port的Instance集合
         * 仅在ips中存在  获取在两者中相同ip:port的Instance集合
         * 仅在ips中存在  获取在两者中相同ip:port的Instance集合
         */
        List<Instance> updatedIPs = updatedIPs(ips, oldIPMap.values());
        if (updatedIPs.size() > 0) {
            for (Instance ip : updatedIPs) {
                Instance oldIP = oldIPMap.get(ip.getDatumKey());

                // do not update the ip validation status of updated ips
                // because the checker has the most precise result
                // Only when ip is not marked, don't we update the health status of IP:
                /**
                 * 不要更新  待更新Instance集合（ips）中的Instance（ip）对应的验证状态
                 * 因为checker拥有最精准的结果？？？？？  checker是否是ClientBeatCheckTask
                 * 只有当前Instance的状态为非marker时   我们才更新其对应的healthy
                 */
                if (!ip.isMarked()) {
                    ip.setHealthy(oldIP.isHealthy());
                }

                if (ip.isHealthy() != oldIP.isHealthy()) {
                    // ip validation status updated
                    Loggers.EVT_LOG.info("{} {SYNC} IP-{} {}:{}@{}",
                        getService().getName(), (ip.isHealthy() ? "ENABLED" : "DISABLED"), ip.getIp(), ip.getPort(), getName());
                }

                if (ip.getWeight() != oldIP.getWeight()) {
                    // ip validation status updated
                    Loggers.EVT_LOG.info("{} {SYNC} {IP-UPDATED} {}->{}", getService().getName(), oldIP.toString(), ip.toString());
                }
            }
        }

        /**
         * ip:port   仅在ips存在   在oldIPMap中不存在的Instance集合
         * 即新增的Instance集合
         */
        List<Instance> newIPs = subtract(ips, oldIPMap.values());
        if (newIPs.size() > 0) {
            Loggers.EVT_LOG.info("{} {SYNC} {IP-NEW} cluster: {}, new ips size: {}, content: {}",
                getService().getName(), getName(), newIPs.size(), newIPs.toString());

            for (Instance ip : newIPs) {
                /**
                 * 重置
                 */
                HealthCheckStatus.reset(ip);
            }
        }

        /**
         * ip:port   仅在oldIPMap存在   在ips中不存在的Instance集合
         * 即已经失效（移除）的Instance集合
         */
        List<Instance> deadIPs = subtract(oldIPMap.values(), ips);

        if (deadIPs.size() > 0) {
            Loggers.EVT_LOG.info("{} {SYNC} {IP-DEAD} cluster: {}, dead ips size: {}, content: {}",
                getService().getName(), getName(), deadIPs.size(), deadIPs.toString());

            for (Instance ip : deadIPs) {
                /**
                 * 删除
                 */
                HealthCheckStatus.remv(ip);
            }
        }

        toUpdateInstances = new HashSet<>(ips);

        /**
         * 修改当前cluster对应的Instance集合
         */
        if (ephemeral) {
            ephemeralInstances = toUpdateInstances;
        } else {
            persistentInstances = toUpdateInstances;
        }
    }

    /**
     * 仅在a中的元素   且对应的ip:port在a和b中都有的Instance
     *
     * @param a
     * @param b
     * @return
     */
    public List<Instance> updatedIPs(Collection<Instance> a, Collection<Instance> b) {

        /**
         * ab集合的交集
         */
        List<Instance> intersects = (List<Instance>) CollectionUtils.intersection(a, b);
        Map<String, Instance> stringIPAddressMap = new ConcurrentHashMap<>(intersects.size());

        /**
         * 交集map   以ip:port为key   如果两个instance的ip:port相同  则后者覆盖前者
         */
        for (Instance instance : intersects) {
            stringIPAddressMap.put(instance.getIp() + ":" + instance.getPort(), instance);
        }

        /**
         * ab集合中的元素   且对应的ip:port在两个集合中同时存在  但元素本身未必在ab中同时存在
         * 当value=2时，即当前instance在a和b中都存在
         * 当value=1时，或者在a存在，或者在b存在
         */
        Map<String, Integer> intersectMap = new ConcurrentHashMap<>(a.size() + b.size());
        /**
         * 仅在a中的元素   且对应的ip:port在a和b中都有的元素
         */
        Map<String, Instance> instanceMap = new ConcurrentHashMap<>(a.size());
        /**
         * 集合a中的元素
         */
        Map<String, Instance> instanceMap1 = new ConcurrentHashMap<>(a.size());

        /**
         * 集合b中的元素   且ip:port在交集map中存在
         */
        for (Instance instance : b) {
            if (stringIPAddressMap.containsKey(instance.getIp() + ":" + instance.getPort())) {
                /**
                 * 在b中存在
                 */
                intersectMap.put(instance.toString(), 1);
            }
        }

        /**
         * 集合a中的元素  且在交集map中存在
         */
        for (Instance instance : a) {
            if (stringIPAddressMap.containsKey(instance.getIp() + ":" + instance.getPort())) {

                /**
                 * 在a和b中都存在
                 */
                if (intersectMap.containsKey(instance.toString())) {
                    intersectMap.put(instance.toString(), 2);
                } else {
                    /**
                     * 在a中存在
                     */
                    intersectMap.put(instance.toString(), 1);
                }
            }

            /**
             * 记录集合a的元素
             */
            instanceMap1.put(instance.toString(), instance);

        }

        /**
         * 记录a中的元素
         */
        for (Map.Entry<String, Integer> entry : intersectMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            if (value == 1) {
                /**
                 * 仅在a中的元素
                 */
                if (instanceMap1.containsKey(key)) {
                    instanceMap.put(key, instanceMap1.get(key));
                }
            }
        }

        return new ArrayList<>(instanceMap.values());
    }

    /**
     * ip:port  仅在a中有   在b中没有的Instance集合
     * @param a
     * @param b
     * @return
     */
    public List<Instance> subtract(Collection<Instance> a, Collection<Instance> b) {
        Map<String, Instance> mapa = new HashMap<>(b.size());
        for (Instance o : b) {
            mapa.put(o.getIp() + ":" + o.getPort(), o);
        }

        List<Instance> result = new ArrayList<>();

        for (Instance o : a) {
            if (!mapa.containsKey(o.getIp() + ":" + o.getPort())) {
                result.add(o);
            }
        }

        return result;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getName())
            .append(service)
            .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Cluster cluster = (Cluster) o;

        return new EqualsBuilder()
            .append(getName(), cluster.getName())
            .append(service, cluster.service)
            .isEquals();
    }

    public int getDefCkport() {
        return defCkport;
    }

    public void setDefCkport(int defCkport) {
        this.defCkport = defCkport;
    }

    /**
     * 更新Cluster数据
     * @param cluster
     */
    public void update(Cluster cluster) {

        if (!getHealthChecker().equals(cluster.getHealthChecker())) {
            Loggers.SRV_LOG.info("[CLUSTER-UPDATE] {}:{}:, healthChecker: {} -> {}",
                getService().getName(), getName(), getHealthChecker().toString(), cluster.getHealthChecker().toString());
            setHealthChecker(cluster.getHealthChecker());
        }

        if (defCkport != cluster.getDefCkport()) {
            Loggers.SRV_LOG.info("[CLUSTER-UPDATE] {}:{}, defCkport: {} -> {}",
                getService().getName(), getName(), defCkport, cluster.getDefCkport());
            defCkport = cluster.getDefCkport();
        }

        if (defIPPort != cluster.getDefIPPort()) {
            Loggers.SRV_LOG.info("[CLUSTER-UPDATE] {}:{}, defIPPort: {} -> {}",
                getService().getName(), getName(), defIPPort, cluster.getDefIPPort());
            defIPPort = cluster.getDefIPPort();
        }

        if (!StringUtils.equals(sitegroup, cluster.getSitegroup())) {
            Loggers.SRV_LOG.info("[CLUSTER-UPDATE] {}:{}, sitegroup: {} -> {}",
                getService().getName(), getName(), sitegroup, cluster.getSitegroup());
            sitegroup = cluster.getSitegroup();
        }

        if (isUseIPPort4Check() != cluster.isUseIPPort4Check()) {
            Loggers.SRV_LOG.info("[CLUSTER-UPDATE] {}:{}, useIPPort4Check: {} -> {}",
                getService().getName(), getName(), isUseIPPort4Check(), cluster.isUseIPPort4Check());
            setUseIPPort4Check(cluster.isUseIPPort4Check());
        }

        metadata = cluster.getMetadata();
    }

    public String getSitegroup() {
        return sitegroup;
    }

    public void setSitegroup(String sitegroup) {
        this.sitegroup = sitegroup;
    }

    public boolean contains(Instance ip) {
        return persistentInstances.contains(ip) || ephemeralInstances.contains(ip);
    }

    /**
     * validate the current cluster.
     * <p>the cluster name cannot be null, and only the arabic numerals, letters and endashes are allowed.
     *
     * @throws IllegalArgumentException the service is null, or the cluster name is null, or the cluster name is illegal
     */
    public void validate() {
        Assert.notNull(getName(), "cluster name cannot be null");
        Assert.notNull(service, "service cannot be null");
        if (!getName().matches(CLUSTER_NAME_SYNTAX)) {
            throw new IllegalArgumentException("cluster name can only have these characters: 0-9a-zA-Z-, current: " + getName());
        }
    }
}
