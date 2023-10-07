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

package com.alibaba.nacos.client.naming.backups.datasource;

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.client.naming.backups.FailoverData;
import com.alibaba.nacos.client.naming.backups.FailoverDataSource;
import com.alibaba.nacos.client.naming.backups.FailoverSwitch;
import com.alibaba.nacos.client.naming.backups.NamingFailoverData;
import com.alibaba.nacos.client.naming.cache.ConcurrentDiskUtil;
import com.alibaba.nacos.client.naming.cache.DiskCache;
import com.alibaba.nacos.client.naming.cache.ServiceInfoHolder;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.alibaba.nacos.client.naming.utils.UtilAndComs;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

/**
 * Failover Data Disk Impl.
 *
 * @author zongkang.guo
 */
public class DiskFailoverDataSource implements FailoverDataSource {

    private static final String FAILOVER_DIR = "/failover";

    private static final String IS_FAILOVER_MODE = "1";

    private static final String NO_FAILOVER_MODE = "0";

    private Map<String, FailoverData> serviceMap = new ConcurrentHashMap<>();

    private Set<String> serviceNames = new HashSet<>();

    private String failoverDir;

    private long lastModifiedMillis = 0L;

    public DiskFailoverDataSource(ServiceInfoHolder serviceInfoHolder) {
        this.failoverDir = serviceInfoHolder.getCacheDir() + FAILOVER_DIR;
    }

    class FailoverFileReader implements Runnable {

        @Override
        public void run() {
            Map<String, FailoverData> domMap = new HashMap<>(200);

            BufferedReader reader = null;
            try {

                File cacheDir = new File(failoverDir);
                if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                    throw new IllegalStateException("failed to create cache dir: " + failoverDir);
                }

                File[] files = cacheDir.listFiles();
                if (files == null) {
                    return;
                }

                for (File file : files) {
                    if (!file.isFile()) {
                        continue;
                    }

                    if (file.getName().equals(UtilAndComs.FAILOVER_SWITCH)) {
                        continue;
                    }

                    if (file.getName().equals(UtilAndComs.FAILOVER_SERVICENAME)) {
                        try {
                            String dataString = ConcurrentDiskUtil.getFileContent(file,
                                    Charset.defaultCharset().toString());
                            reader = new BufferedReader(new StringReader(dataString));
                            String serviceNameStr;
                            if ((serviceNameStr = reader.readLine()) != null) {
                                String[] serviceNameList = serviceNameStr.split(",");
                                if (serviceNameList.length > 0) {
                                    serviceNames = new HashSet<>(Arrays.asList(serviceNameList));
                                }
                            }
                            continue;
                        } catch (Exception e) {
                            NAMING_LOGGER.error("[NA] failed to read cache for dom: {}", file.getName(), e);
                            continue;
                        } finally {
                            try {
                                if (reader != null) {
                                    reader.close();
                                }
                            } catch (Exception e) {
                                //ignore
                            }
                        }
                    }

                    ServiceInfo dom = null;

                    try {
                        dom = new ServiceInfo(URLDecoder.decode(file.getName(), StandardCharsets.UTF_8.name()));
                        String dataString = ConcurrentDiskUtil.getFileContent(file,
                                Charset.defaultCharset().toString());
                        reader = new BufferedReader(new StringReader(dataString));

                        String json;
                        if ((json = reader.readLine()) != null) {
                            try {
                                dom = JacksonUtils.toObj(json, ServiceInfo.class);
                            } catch (Exception e) {
                                NAMING_LOGGER.error("[NA] error while parsing cached dom : {}", json, e);
                            }
                        }

                    } catch (Exception e) {
                        NAMING_LOGGER.error("[NA] failed to read cache for dom: {}", file.getName(), e);
                    } finally {
                        try {
                            if (reader != null) {
                                reader.close();
                            }
                        } catch (Exception e) {
                            //ignore
                        }
                    }
                    if (dom != null && !CollectionUtils.isEmpty(dom.getHosts())) {
                        domMap.put(dom.getKey(), NamingFailoverData.newNamingFailoverData(dom));
                    }
                }
            } catch (Exception e) {
                NAMING_LOGGER.error("[NA] failed to read cache file", e);
            }

            if (domMap.size() > 0) {
                serviceMap = domMap;
            }
        }
    }

    @Override
    public FailoverSwitch getSwitch() {
        try {
            File switchFile = Paths.get(failoverDir, UtilAndComs.FAILOVER_SWITCH).toFile();
            if (!switchFile.exists()) {
                NAMING_LOGGER.debug("failover switch is not found, {}", switchFile.getName());
                return new FailoverSwitch(Boolean.FALSE);
            }

            long modified = switchFile.lastModified();

            if (lastModifiedMillis < modified) {
                lastModifiedMillis = modified;
                String failover = ConcurrentDiskUtil.getFileContent(switchFile.getPath(),
                        Charset.defaultCharset().toString());
                if (!StringUtils.isEmpty(failover)) {
                    String[] lines = failover.split(DiskCache.getLineSeparator());

                    for (String line : lines) {
                        String line1 = line.trim();
                        if (IS_FAILOVER_MODE.equals(line1)) {
                            NAMING_LOGGER.info("failover-mode is on");
                            new FailoverFileReader().run();
                            return new FailoverSwitch(Boolean.TRUE, serviceNames);
                        } else if (NO_FAILOVER_MODE.equals(line1)) {
                            NAMING_LOGGER.info("failover-mode is off");
                        }
                    }
                }
            }

        } catch (Throwable e) {
            NAMING_LOGGER.error("[NA] failed to read failover switch.", e);
        }

        return new FailoverSwitch(Boolean.FALSE);
    }

    @Override
    public Map<String, FailoverData> getFailoverData() {
        return serviceMap;
    }

}