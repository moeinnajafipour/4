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

package com.alibaba.nacos.core.code;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.executor.ThreadPoolManager;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.file.WatchFileCenter;
import com.alibaba.nacos.sys.utils.ApplicationUtils;
import com.alibaba.nacos.sys.utils.DiskUtils;
import com.alibaba.nacos.sys.utils.InetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.springframework.boot.context.logging.LoggingApplicationListener.CONFIG_PROPERTY;
import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;

/**
 * Nacos Application Listener, execute init process.
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @since 0.5.0
 */
public class NacosApplicationListener {
    
    private static final String DEFAULT_NACOS_LOGBACK_LOCATION = CLASSPATH_URL_PREFIX + "META-INF/logback/nacos.xml";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosApplicationListener.class);
    
    private static final String MODE_PROPERTY_KEY_STAND_MODE = "nacos.mode";
    
    private static final String MODE_PROPERTY_KEY_FUNCTION_MODE = "nacos.function.mode";
    
    private static final String LOCAL_IP_PROPERTY_KEY = "nacos.local.ip";
    
    private static ScheduledExecutorService scheduledExecutorService;
    
    private static volatile boolean starting;
    
    /**
     * {@link SpringApplicationRunListener#starting}.
     */
    public static void starting() {
        starting = true;
    }
    
    /**
     * {@link com.alibaba.nacos.core.code.SpringApplicationRunListener#environmentPrepared}.
     *
     * @param environment environment
     */
    public static void environmentPrepared(ConfigurableEnvironment environment) {
        setUpLogConf(environment);
        
        injectEnvironment(environment);
        
        loadPreProperties(environment);
        
        initSystemProperty();
    }
    
    /**
     * {@link com.alibaba.nacos.core.code.SpringApplicationRunListener#contextLoaded}.
     *
     * @param context context
     */
    public static void contextPrepared(ConfigurableApplicationContext context) {
        logClusterConf();
        logStarting();
    }
    
    /**
     * {@link com.alibaba.nacos.core.code.SpringApplicationRunListener#contextLoaded}.
     *
     * @param context context
     */
    public static void contextLoaded(ConfigurableApplicationContext context) {
    
    }
    
    /**
     * {@link com.alibaba.nacos.core.code.SpringApplicationRunListener#started}.
     *
     * @param context context
     */
    public static void started(ConfigurableApplicationContext context) {
        starting = false;
        
        closeExecutor();
        
        logFilePath();
        
        judgeStorageMode(context.getEnvironment());
    }
    
    /**
     * {@link com.alibaba.nacos.core.code.SpringApplicationRunListener#running}.
     *
     * @param context context
     */
    public static void running(ConfigurableApplicationContext context) {
        removePreProperties(context.getEnvironment());
    }
    
    /**
     * {@link com.alibaba.nacos.core.code.SpringApplicationRunListener#failed}.
     *
     * @param context   context
     * @param exception exception
     */
    public static void failed(ConfigurableApplicationContext context, Throwable exception) {
        starting = false;
        
        logFilePath();
        
        LOGGER.error("Startup errors : {}", exception);
        ThreadPoolManager.shutdown();
        WatchFileCenter.shutdown();
        NotifyCenter.shutdown();
        
        closeExecutor();
        
        context.close();
        
        LOGGER.error("Nacos failed to start, please see {} for more details.",
                Paths.get(ApplicationUtils.getNacosHome(), "logs/nacos.log"));
    }
    
    private static void setUpLogConf(ConfigurableEnvironment environment) {
        if (!environment.containsProperty(CONFIG_PROPERTY)) {
            System.setProperty(CONFIG_PROPERTY, DEFAULT_NACOS_LOGBACK_LOCATION);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("There is no property named \"{}\" in Spring Boot Environment, "
                                + "and whose value is {} will be set into System's Properties", CONFIG_PROPERTY,
                        DEFAULT_NACOS_LOGBACK_LOCATION);
            }
        }
    }
    
    private static void injectEnvironment(ConfigurableEnvironment environment) {
        ApplicationUtils.injectEnvironment(environment);
        EnvUtil.setEnvironment(environment);
    }
    
    private static void loadPreProperties(ConfigurableEnvironment environment) {
        try {
            environment.getPropertySources().addLast(new OriginTrackedMapPropertySource("first_pre",
                    EnvUtil.loadProperties(EnvUtil.getApplicationConfFileResource())));
        } catch (IOException e) {
            throw new NacosRuntimeException(NacosException.SERVER_ERROR, e);
        }
    }
    
    private static void initSystemProperty() {
        if (ApplicationUtils.getStandaloneMode()) {
            System.setProperty(MODE_PROPERTY_KEY_STAND_MODE, "stand alone");
        } else {
            System.setProperty(MODE_PROPERTY_KEY_STAND_MODE, "cluster");
        }
        if (ApplicationUtils.getFunctionMode() == null) {
            System.setProperty(MODE_PROPERTY_KEY_FUNCTION_MODE, "All");
        } else if (ApplicationUtils.FUNCTION_MODE_CONFIG.equals(ApplicationUtils.getFunctionMode())) {
            System.setProperty(MODE_PROPERTY_KEY_FUNCTION_MODE, ApplicationUtils.FUNCTION_MODE_CONFIG);
        } else if (ApplicationUtils.FUNCTION_MODE_NAMING.equals(ApplicationUtils.getFunctionMode())) {
            System.setProperty(MODE_PROPERTY_KEY_FUNCTION_MODE, ApplicationUtils.FUNCTION_MODE_NAMING);
        }
        
        System.setProperty(LOCAL_IP_PROPERTY_KEY, InetUtils.getSelfIP());
    }
    
    private static void removePreProperties(ConfigurableEnvironment environment) {
        environment.getPropertySources().remove("first_pre");
    }
    
    private static void logClusterConf() {
        if (!ApplicationUtils.getStandaloneMode()) {
            try {
                List<String> clusterConf = ApplicationUtils.readClusterConf();
                LOGGER.info("The server IP list of Nacos is {}", clusterConf);
            } catch (IOException e) {
                LOGGER.error("read cluster conf fail", e);
            }
        }
    }
    
    private static void closeExecutor() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }
    }
    
    private static void logFilePath() {
        String[] dirNames = new String[] {"logs", "conf", "data"};
        for (String dirName : dirNames) {
            LOGGER.info("Nacos Log files: {}", Paths.get(ApplicationUtils.getNacosHome(), dirName).toString());
            try {
                DiskUtils.forceMkdir(new File(Paths.get(ApplicationUtils.getNacosHome(), dirName).toUri()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private static void logStarting() {
        if (!ApplicationUtils.getStandaloneMode()) {
            
            scheduledExecutorService = ExecutorFactory
                    .newSingleScheduledExecutorService(new NameThreadFactory("com.alibaba.nacos.core.nacos-starting"));
            
            scheduledExecutorService.scheduleWithFixedDelay(() -> {
                if (starting) {
                    LOGGER.info("Nacos is starting...");
                }
            }, 1, 1, TimeUnit.SECONDS);
        }
    }
    
    private static void judgeStorageMode(ConfigurableEnvironment env) {
        
        // External data sources are used by default in cluster mode
        boolean useExternalStorage = ("mysql".equalsIgnoreCase(env.getProperty("spring.datasource.platform", "")));
        
        // must initialize after setUseExternalDB
        // This value is true in stand-alone mode and false in cluster mode
        // If this value is set to true in cluster mode, nacos's distributed storage engine is turned on
        // default value is depend on ${nacos.standalone}
        
        if (!useExternalStorage) {
            boolean embeddedStorage = ApplicationUtils.getStandaloneMode() || Boolean.getBoolean("embeddedStorage");
            // If the embedded data source storage is not turned on, it is automatically
            // upgraded to the external data source storage, as before
            if (!embeddedStorage) {
                useExternalStorage = true;
            }
        }
        
        LOGGER.info("Nacos started successfully in {} mode. use {} storage",
                System.getProperty(MODE_PROPERTY_KEY_STAND_MODE), useExternalStorage ? "external" : "embedded");
    }
}
