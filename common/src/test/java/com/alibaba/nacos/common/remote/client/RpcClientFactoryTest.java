/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.remote.client;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.remote.ConnectionType;
import com.alibaba.nacos.common.utils.CollectionUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RpcClientFactoryTest {
    
    static Field clientMapField;
    
    @Mock
    RpcClient rpcClient;
    
    @Mock(lenient = true)
    RpcClientTlsConfig tlsConfig;
    
    @BeforeClass
    public static void setUpBeforeClass() throws NoSuchFieldException, IllegalAccessException {
        clientMapField = RpcClientFactory.class.getDeclaredField("CLIENT_MAP");
        clientMapField.setAccessible(true);
        Field modifiersField1 = Field.class.getDeclaredField("modifiers");
        modifiersField1.setAccessible(true);
        modifiersField1.setInt(clientMapField, clientMapField.getModifiers() & ~Modifier.FINAL);
    }
    
    @After
    public void tearDown() throws IllegalAccessException {
        clientMapField.set(null, new ConcurrentHashMap<>());
    }
    
    @Test
    public void testGetUnmodifiableClientEntries() throws IllegalAccessException {
        assertTrue(RpcClientFactory.getUnmodifiableClientEntries().isEmpty());
        
        clientMapField.set(null, Collections.singletonMap("testClient", rpcClient));
        assertEquals(1, RpcClientFactory.getUnmodifiableClientEntries().size());
    }
    
    @Test
    public void testUnmodifiableClientEntries() throws IllegalAccessException {
        Map<String, RpcClient> clientMap = new HashMap<>();
        clientMap.put("testClient1", rpcClient);
        clientMap.put("testClient2", rpcClient);
        clientMapField.set(null, clientMap);
        
        Set<Map.Entry<String, RpcClient>> unmodifiableClientEntries = RpcClientFactory.getUnmodifiableClientEntries();
        
        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableClientEntries.remove(unmodifiableClientEntries.iterator().next()));
        
        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableClientEntries.add(new AbstractMap.SimpleEntry<>("NewClient", rpcClient)));
        
        assertEquals(unmodifiableClientEntries.size(), 2);
    }
    
    @Test
    public void testDestroyClientWhenClientExistThenRemoveAndShutDownRpcClient()
            throws IllegalAccessException, NacosException {
        clientMapField.set(null, new ConcurrentHashMap<>(Collections.singletonMap("testClient", rpcClient)));
        
        RpcClientFactory.destroyClient("testClient");
        
        assertTrue(RpcClientFactory.getUnmodifiableClientEntries().isEmpty());
        verify(rpcClient).shutdown();
    }
    
    @Test
    public void testDestroyClientByPredicateWhenClientExistThenRemoveAndShutDownRpcClient()
            throws IllegalAccessException, NacosException {
        clientMapField.set(null, new ConcurrentHashMap<>(Collections.singletonMap("testClient", rpcClient)));
        
        Predicate<Map.Entry<String, RpcClient>> predicate = entry -> "testClient".equals(entry.getKey());
        List<String> destroyClients = RpcClientFactory.destroyClient(predicate);
        assertEquals(destroyClients, Collections.singletonList("testClient"));
        
        assertTrue(RpcClientFactory.getUnmodifiableClientEntries().isEmpty());
        verify(rpcClient).shutdown();
    }
    
    @Test
    public void testDestroyClientWhenClientNotExistThenDoNothing() throws IllegalAccessException, NacosException {
        clientMapField.set(null, new ConcurrentHashMap<>(Collections.singletonMap("testClient", rpcClient)));
        
        RpcClientFactory.destroyClient("notExistClientName");
        
        Map.Entry<String, RpcClient> element = CollectionUtils.getOnlyElement(
                RpcClientFactory.getUnmodifiableClientEntries());
        assertEquals("testClient", element.getKey());
        assertEquals(rpcClient, element.getValue());
        verify(rpcClient, times(0)).shutdown();
    }
    
    @Test
    public void testGetClient() throws IllegalAccessException {
        // may be null
        Assert.assertNull(RpcClientFactory.getClient("notExistClientName"));
        
        clientMapField.set(null, new ConcurrentHashMap<>(Collections.singletonMap("testClient", rpcClient)));
        assertEquals(rpcClient, RpcClientFactory.getClient("testClient"));
    }
    
    @Test
    public void testCreateClientWhenNotCreatedThenCreate() {
        RpcClient client = RpcClientFactory.createClient("testClient", ConnectionType.GRPC,
                Collections.singletonMap("labelKey", "labelValue"));
        Map<String, String> labesMap = new HashMap<>();
        labesMap.put("labelKey", "labelValue");
        labesMap.put("tls.enable", "false");
        assertEquals(labesMap, client.rpcClientConfig.labels());
        assertEquals(ConnectionType.GRPC, client.getConnectionType());
        assertEquals("testClient",
                CollectionUtils.getOnlyElement(RpcClientFactory.getUnmodifiableClientEntries()).getKey());
    }
    
    @Test
    public void testCreateClientWhenAlreadyCreatedThenNotCreateAgain() {
        RpcClient client1 = RpcClientFactory.createClient("testClient", ConnectionType.GRPC,
                Collections.singletonMap("labelKey", "labelValue"));
        RpcClient client2 = RpcClientFactory.createClient("testClient", ConnectionType.GRPC,
                Collections.singletonMap("labelKey", "labelValue"));
        
        assertEquals(client1, client2);
        assertEquals(1, RpcClientFactory.getUnmodifiableClientEntries().size());
    }
    
    @Test(expected = Exception.class)
    public void testCreatedClientWhenConnectionTypeNotMappingThenThrowException() {
        RpcClientFactory.createClient("testClient", mock(ConnectionType.class),
                Collections.singletonMap("labelKey", "labelValue"));
    }
    
    @Test
    public void testCreateClusterClientWhenNotCreatedThenCreate() {
        RpcClient client = RpcClientFactory.createClusterClient("testClient", ConnectionType.GRPC,
                Collections.singletonMap("labelKey", "labelValue"));
        Map<String, String> labesMap = new HashMap<>();
        labesMap.put("labelKey", "labelValue");
        labesMap.put("tls.enable", "false");
        assertEquals(labesMap, client.rpcClientConfig.labels());
        assertEquals(ConnectionType.GRPC, client.getConnectionType());
        assertEquals("testClient",
                CollectionUtils.getOnlyElement(RpcClientFactory.getUnmodifiableClientEntries()).getKey());
    }
    
    @Test
    public void testCreateClusterClientWhenAlreadyCreatedThenNotCreateAgain() {
        RpcClient client1 = RpcClientFactory.createClusterClient("testClient", ConnectionType.GRPC,
                Collections.singletonMap("labelKey", "labelValue"));
        RpcClient client2 = RpcClientFactory.createClusterClient("testClient", ConnectionType.GRPC,
                Collections.singletonMap("labelKey", "labelValue"));
        
        assertEquals(client1, client2);
        assertEquals(1, RpcClientFactory.getUnmodifiableClientEntries().size());
    }
    
    @Test(expected = Exception.class)
    public void testCreatedClusterClientWhenConnectionTypeNotMappingThenThrowException() {
        RpcClientFactory.createClusterClient("testClient", mock(ConnectionType.class),
                Collections.singletonMap("labelKey", "labelValue"));
    }
    
    @Test
    public void testCreateClusterClientTsl() {
        Mockito.when(tlsConfig.getEnableTls()).thenReturn(true);
        RpcClient client = RpcClientFactory.createClusterClient("testClient", ConnectionType.GRPC,
                Collections.singletonMap("labelKey", "labelValue"), tlsConfig);
        Map<String, String> labesMap = new HashMap<>();
        labesMap.put("labelKey", "labelValue");
        labesMap.put("tls.enable", "true");
        assertEquals(labesMap, client.rpcClientConfig.labels());
        assertEquals(ConnectionType.GRPC, client.getConnectionType());
        assertEquals("testClient",
                CollectionUtils.getOnlyElement(RpcClientFactory.getUnmodifiableClientEntries()).getKey());
    }
    
    @Test
    public void testCreateClientTsl() {
        Mockito.when(tlsConfig.getEnableTls()).thenReturn(true);
        RpcClient client = RpcClientFactory.createClient("testClient", ConnectionType.GRPC,
                Collections.singletonMap("labelKey", "labelValue"), tlsConfig);
        Map<String, String> labesMap = new HashMap<>();
        labesMap.put("labelKey", "labelValue");
        labesMap.put("tls.enable", "true");
        assertEquals(labesMap, client.rpcClientConfig.labels());
        assertEquals(ConnectionType.GRPC, client.getConnectionType());
        assertEquals("testClient",
                CollectionUtils.getOnlyElement(RpcClientFactory.getUnmodifiableClientEntries()).getKey());
    }
}