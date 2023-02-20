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


package com.alibaba.nacos.test.config;

import com.alibaba.nacos.Nacos;
import com.alibaba.nacos.api.config.remote.request.ConfigPublishRequest;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.auth.config.AuthConfigs;
import com.alibaba.nacos.common.remote.ConnectionType;
import com.alibaba.nacos.common.remote.client.Connection;
import com.alibaba.nacos.common.remote.client.RpcClient;
import com.alibaba.nacos.common.remote.client.RpcClientFactory;
import com.alibaba.nacos.common.remote.client.grpc.TlsConfig;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.utils.InetUtils;
import com.alibaba.nacos.test.base.ConfigCleanUtils;
import org.junit.*;
import org.junit.runner.OrderWith;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Alphanumeric;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * use  configPublishRequest for  communication verification between client and server
 *
 * @author githubcheng2978
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Nacos.class},
        properties = {
                "nacos.standalone=true",
                "server.port=9610",
                "nacos.remote.server.grpc.tls.enableSSL=true",
                "nacos.remote.server.grpc.tls.certificateChainFile=server-cert.pem",
                "nacos.remote.server.grpc.tls.privateKeyFile=server-key.pem",
                "nacos.remote.server.grpc.tlstrustCertCollectionFile"},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ConfigIntegrationV3Test {


    public static AtomicInteger increment = new AtomicInteger(100);

    @BeforeClass
    public static void beforeClass() throws IOException {
        ConfigCleanUtils.changeToNewTestNacosHome(ConfigIntegrationV3Test.class.getSimpleName());
        EnvUtil.setPort(9610);

    }

    @BeforeClass
    @AfterClass
    public static void cleanClientCache() throws Exception {
        ConfigCleanUtils.cleanClientCache();
    }

    @Test
    public void test_e_TlsServerAndPlainClient() throws Exception {
        RpcClient client = RpcClientFactory.createClient("testTlsServerAndPlainClient", ConnectionType.GRPC, Collections.singletonMap("labelKey", "labelValue"), null);
        RpcClient.ServerInfo serverInfo = new RpcClient.ServerInfo();
        serverInfo.setServerIp("127.0.0.1");

        serverInfo.setServerPort(EnvUtil.getPort());
        Connection connection = client.connectToServer(serverInfo);
        ConfigPublishRequest configPublishRequest = new ConfigPublishRequest();

        String content = UUID.randomUUID().toString();

        configPublishRequest.setContent(content);
        configPublishRequest.setGroup("test-group" + increment.getAndIncrement());
        configPublishRequest.setDataId("test-data" + increment.getAndIncrement());
        configPublishRequest.setRequestId(content);
        Response response = connection.request(configPublishRequest, TimeUnit.SECONDS.toMillis(3));
        Assert.assertTrue(response.isSuccess());
        connection.close();

    }

    @Test
    public void test_f_ServerTlsTrustAll() throws Exception {

        TlsConfig tlsConfig = new TlsConfig();
        tlsConfig.setEnableTls(true);
        tlsConfig.setTrustAll(true);
        RpcClient.ServerInfo serverInfo = new RpcClient.ServerInfo();
        serverInfo.setServerIp("127.0.0.1");

        serverInfo.setServerPort(EnvUtil.getPort());
        RpcClient clientTrustAll = RpcClientFactory.createClient("testServerTlsTrustAll", ConnectionType.GRPC, Collections.singletonMap("labelKey", "labelValue"), tlsConfig);
        Connection connectionTrustAll = clientTrustAll.connectToServer(serverInfo);
        ConfigPublishRequest configPublishRequest = new ConfigPublishRequest();
        String content = UUID.randomUUID().toString();
        configPublishRequest.setContent(content);
        configPublishRequest.setGroup("test-group" + increment.getAndIncrement());
        configPublishRequest.setDataId("test-data" + increment.getAndIncrement());
        Response response = connectionTrustAll.request(configPublishRequest, TimeUnit.SECONDS.toMillis(3));
        Assert.assertTrue(response.isSuccess());
        connectionTrustAll.close();

    }

    @Test
    public void test_g_ServerTlsTrustCa() throws Exception {

        RpcClient.ServerInfo serverInfo = new RpcClient.ServerInfo();
        serverInfo.setServerIp("127.0.0.1");

        serverInfo.setServerPort(EnvUtil.getPort());
        TlsConfig tlsConfig = new TlsConfig();
        tlsConfig.setEnableTls(true);
        tlsConfig.setTrustCollectionCertFile("ca-cert.pem");
        RpcClient clientTrustCa = RpcClientFactory.createClient("testServerTlsTrustCa", ConnectionType.GRPC, Collections.singletonMap("labelKey", "labelValue"), tlsConfig);
        Connection connectionTrustCa = clientTrustCa.connectToServer(serverInfo);
        ConfigPublishRequest configPublishRequestCa = new ConfigPublishRequest();
        String contentCa = UUID.randomUUID().toString();

        configPublishRequestCa.setContent(contentCa);
        configPublishRequestCa.setGroup("test-group" + increment.getAndIncrement());
        configPublishRequestCa.setDataId("test-data" + increment.getAndIncrement());
        Response responseCa = connectionTrustCa.request(configPublishRequestCa, TimeUnit.SECONDS.toMillis(3));
        Assert.assertTrue(responseCa.isSuccess());
        connectionTrustCa.close();
    }
}
