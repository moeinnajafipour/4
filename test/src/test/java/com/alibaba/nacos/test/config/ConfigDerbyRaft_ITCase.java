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

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.common.http.HttpClientManager;
import com.alibaba.nacos.common.http.NSyncHttpClient;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.common.model.RestResult;
import com.alibaba.nacos.config.server.model.event.RaftDBErrorEvent;
import com.alibaba.nacos.config.server.model.event.RaftDBErrorRecoverEvent;
import com.alibaba.nacos.config.server.service.repository.EmbeddedStoragePersistServiceImpl;
import com.alibaba.nacos.config.server.service.repository.PersistService;
import com.alibaba.nacos.config.server.service.repository.DistributedDatabaseOperateImpl;
import com.alibaba.nacos.consistency.cp.CPProtocol;
import com.alibaba.nacos.consistency.cp.Constants;
import com.alibaba.nacos.core.distributed.id.IdGeneratorManager;
import com.alibaba.nacos.core.distributed.raft.utils.JRaftConstants;
import com.alibaba.nacos.core.notify.Event;
import com.alibaba.nacos.core.notify.NotifyCenter;
import com.alibaba.nacos.core.notify.listener.Subscribe;
import com.alibaba.nacos.common.utils.DiskUtils;
import com.alibaba.nacos.core.utils.ApplicationUtils;
import com.alibaba.nacos.core.utils.GenericType;
import com.alibaba.nacos.core.utils.InetUtils;
import com.alibaba.nacos.common.utils.ThreadUtils;
import com.alibaba.nacos.test.base.HttpClient4Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.web.context.support.StandardServletEnvironment;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@Ignore
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class ConfigDerbyRaft_ITCase
		extends HttpClient4Test {

	private static final String CONFIG_INFO_ID = "config-info-id";
	private static final String CONFIG_HISTORY_ID = "config-history-id";
	private static final String CONFIG_TAG_RELATION_ID = "config-tag-relation-id";
	private static final String CONFIG_BETA_ID = "config-beta-id";
	private static final String NAMESPACE_ID = "namespace-id";
	private static final String USER_ID = "user-id";
	private static final String ROLE_ID = "role-id";
	private static final String PERMISSION_ID = "permissions-id";

	private static String serverIp7 = "127.0.0.1:8847";
	private static String serverIp8 = "127.0.0.1:8848";
	private static String serverIp9 = "127.0.0.1:8849";

	private static ConfigService iconfig7;
	private static ConfigService iconfig8;
	private static ConfigService iconfig9;

	private static final NSyncHttpClient httpClient = HttpClientManager.getShareSyncHttpClient();

	private static final AtomicBoolean[] finished = new AtomicBoolean[]{new AtomicBoolean(false), new AtomicBoolean(false), new AtomicBoolean(false)};

	private static Map<String, ConfigurableApplicationContext> applications = new HashMap<>();

	private static String clusterInfo;

	static {
		System.getProperties().setProperty("nacos.core.auth.enabled", "false");
		System.getProperties().setProperty("embeddedStorage", "true");
		String ip = InetUtils.getSelfIp();
		clusterInfo = "nacos.member.list=" + ip + ":8847," + ip
				+ ":8848," + ip + ":8849";

		NotifyCenter.registerSubscribe(new Subscribe<RaftDBErrorEvent>() {
			@Override
			public void onEvent(RaftDBErrorEvent event) {
				System.out.println(event.getEx());
			}

			@Override
			public Class<? extends Event> subscribeType() {
				return RaftDBErrorEvent.class;
			}
		});
	}

	@Before
	public void before() throws Exception {

		CountDownLatch latch = new CountDownLatch(3);

		Runnable runnable = () -> {
			for (int i = 0; i < 3; i++) {
				try {
					URL runnerUrl = new File("../console/target/classes").toURI().toURL();
					URL[] urls = new URL[] { runnerUrl };
					URLClassLoader cl = new URLClassLoader(urls);
					Class<?> runnerClass = cl.loadClass("com.alibaba.nacos.Nacos");
					run(i, latch, runnerClass);
				} catch (Exception e) {
					latch.countDown();
				}
			}
		};

		new Thread(runnable).start();

		latch.await();

		System.out.println("The cluster node initialization is complete");

		Properties setting7 = new Properties();
		setting7.put(PropertyKeyConst.SERVER_ADDR, serverIp7);
		setting7.put(PropertyKeyConst.USERNAME, "nacos");
		setting7.put(PropertyKeyConst.PASSWORD, "nacos");
		iconfig7 = NacosFactory.createConfigService(setting7);

		Properties setting8 = new Properties();
		setting8.put(PropertyKeyConst.SERVER_ADDR, serverIp8);
		setting8.put(PropertyKeyConst.USERNAME, "nacos");
		setting8.put(PropertyKeyConst.PASSWORD, "nacos");
		iconfig8 = NacosFactory.createConfigService(setting8);

		Properties setting9 = new Properties();
		setting9.put(PropertyKeyConst.SERVER_ADDR, serverIp9);
		setting9.put(PropertyKeyConst.USERNAME, "nacos");
		setting9.put(PropertyKeyConst.PASSWORD, "nacos");
		iconfig9 = NacosFactory.createConfigService(setting9);

		TimeUnit.SECONDS.sleep(20L);
	}

	@After
	public void after() throws Exception {
		CountDownLatch latch = new CountDownLatch(applications.size());
		for (ConfigurableApplicationContext context : applications.values()) {
			new Thread(() -> {
				try {
					System.out.println("start close : " + context);
					context.close();
				} catch (Exception ignore) {
				} finally {
					System.out.println("finished close : " + context);
					latch.countDown();
				}
			}).start();
		}
		latch.await();
	}

	@Test
	public void test_a_publish_config() throws Exception {
		boolean result = iconfig7.publishConfig("raft_test", "cluster_test_1",
				"this.is.raft_cluster=lessspring_7");
		Assert.assertTrue(result);

		ThreadUtils.sleep(5000);

		ConfigurableApplicationContext context7 = applications.get("8847");
		ConfigurableApplicationContext context8 = applications.get("8848");
		ConfigurableApplicationContext context9 = applications.get("8849");

		PersistService operate7 = context7.getBean(EmbeddedStoragePersistServiceImpl.class);
		PersistService operate8 = context8.getBean(EmbeddedStoragePersistServiceImpl.class);
		PersistService operate9 = context9.getBean(EmbeddedStoragePersistServiceImpl.class);

		String s7 = operate7.findConfigInfo("raft_test", "cluster_test_1", "").getContent();
		String s8 = operate8.findConfigInfo("raft_test", "cluster_test_1", "").getContent();
		String s9 = operate9.findConfigInfo("raft_test", "cluster_test_1", "").getContent();

		Assert.assertArrayEquals("The three nodes must have consistent data",
				new String[] { s7, s8, s9 },
				new String[] { "this.is.raft_cluster=lessspring_7",
						"this.is.raft_cluster=lessspring_7",
						"this.is.raft_cluster=lessspring_7" });

	}

	@Test
	public void test_b_publish_config() throws Exception {
		ThreadUtils.sleep(5000);

		boolean result = iconfig8.publishConfig("raft_test", "cluster_test_2",
				"this.is.raft_cluster=lessspring_8");
		Assert.assertTrue(result);

		ThreadUtils.sleep(5000);

		ConfigurableApplicationContext context7 = applications.get("8847");
		ConfigurableApplicationContext context8 = applications.get("8848");
		ConfigurableApplicationContext context9 = applications.get("8849");

		PersistService operate7 = context7.getBean(EmbeddedStoragePersistServiceImpl.class);
		PersistService operate8 = context8.getBean(EmbeddedStoragePersistServiceImpl.class);
		PersistService operate9 = context9.getBean(EmbeddedStoragePersistServiceImpl.class);

		String s7 = operate7.findConfigInfo("raft_test", "cluster_test_2", "").getContent();
		String s8 = operate8.findConfigInfo("raft_test", "cluster_test_2", "").getContent();
		String s9 = operate9.findConfigInfo("raft_test", "cluster_test_2", "").getContent();

		Assert.assertArrayEquals("The three nodes must have consistent data",
				new String[] { s7, s8, s9 },
				new String[] { "this.is.raft_cluster=lessspring_8",
						"this.is.raft_cluster=lessspring_8",
						"this.is.raft_cluster=lessspring_8" });
	}

	@Test
	public void test_c_publish_config() throws Exception {
		ThreadUtils.sleep(5000);
		boolean result = iconfig9.publishConfig("raft_test", "cluster_test_2",
				"this.is.raft_cluster=lessspring_9");
		Assert.assertTrue(result);

		ThreadUtils.sleep(5000);

		ConfigurableApplicationContext context7 = applications.get("8847");
		ConfigurableApplicationContext context8 = applications.get("8848");
		ConfigurableApplicationContext context9 = applications.get("8849");

		PersistService operate7 = context7.getBean(EmbeddedStoragePersistServiceImpl.class);
		PersistService operate8 = context8.getBean(EmbeddedStoragePersistServiceImpl.class);
		PersistService operate9 = context9.getBean(EmbeddedStoragePersistServiceImpl.class);

		String s7 = operate7.findConfigInfo("raft_test", "cluster_test_2", "").getContent();
		String s8 = operate8.findConfigInfo("raft_test", "cluster_test_2", "").getContent();
		String s9 = operate9.findConfigInfo("raft_test", "cluster_test_2", "").getContent();

		Assert.assertArrayEquals("The three nodes must have consistent data",
				new String[] { s7, s8, s9 },
				new String[] { "this.is.raft_cluster=lessspring_9",
						"this.is.raft_cluster=lessspring_9",
						"this.is.raft_cluster=lessspring_9" });
	}

	@Test
	public void test_d_modify_config() throws Exception {
		boolean result = iconfig7.publishConfig("raft_test", "cluster_test_1",
				"this.is.raft_cluster=lessspring_7_it_is_for_modify");
		Assert.assertTrue(result);

		ThreadUtils.sleep(5000);

		ConfigurableApplicationContext context7 = applications.get("8847");
		ConfigurableApplicationContext context8 = applications.get("8848");
		ConfigurableApplicationContext context9 = applications.get("8849");

		PersistService operate7 = context7.getBean(EmbeddedStoragePersistServiceImpl.class);
		PersistService operate8 = context8.getBean(EmbeddedStoragePersistServiceImpl.class);
		PersistService operate9 = context9.getBean(EmbeddedStoragePersistServiceImpl.class);

		String s7 = operate7.findConfigInfo("raft_test", "cluster_test_1", "").getContent();
		String s8 = operate8.findConfigInfo("raft_test", "cluster_test_1", "").getContent();
		String s9 = operate9.findConfigInfo("raft_test", "cluster_test_1", "").getContent();

		Assert.assertArrayEquals("The three nodes must have consistent data",
				new String[] { s7, s8, s9 },
				new String[] { "this.is.raft_cluster=lessspring_7_it_is_for_modify",
						"this.is.raft_cluster=lessspring_7_it_is_for_modify",
						"this.is.raft_cluster=lessspring_7_it_is_for_modify" });
	}

	@Test
	public void test_e_derby_ops() throws Exception {
		String url = "http://127.0.0.1:8848/nacos/v1/cs/ops/derby";
		Query query = Query.newInstance()
				.addParam("sql", "select * from users");
		RestResult<List<Map<String, Object>>> result = httpClient.get(url, Header.EMPTY, query, new GenericType<RestResult<List<Map<String, Object>>>>(){}.getType());
		System.out.println(result.getData());
		Assert.assertTrue(result.ok());
		List<Map<String, Object>> list = result.getData();
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("nacos", list.get(0).get("USERNAME"));
	}

	@Test
	public void test_g_derby_ops_no_select() throws Exception {
		String url = "http://127.0.0.1:8848/nacos/v1/cs/ops/derby";
		Query query = Query.newInstance()
				.addParam("sql", "update users set username='nacos'");
		RestResult<Object> result = httpClient.get(url, Header.EMPTY, query, new GenericType<RestResult<Object>>(){}.getType());
		System.out.println(result);
		Assert.assertFalse(result.ok());
		Assert.assertEquals("Only query statements are allowed to be executed", result.getMessage());
	}

	@Test
	public void test_h_derby_has_error() throws Exception {

		ThreadUtils.sleep(5000);

		boolean result = iconfig7.publishConfig("raft_test_raft_error", "cluster_test_1",
				"this.is.raft_cluster=lessspring_7");
		Assert.assertTrue(result);

		NotifyCenter.registerToPublisher(RaftDBErrorRecoverEvent::new, RaftDBErrorRecoverEvent.class, 8);

		CountDownLatch latch1 = new CountDownLatch(1);
		NotifyCenter.registerSubscribe(new Subscribe<RaftDBErrorEvent>() {
			@Override
			public void onEvent(RaftDBErrorEvent event) {
				latch1.countDown();
			}

			@Override
			public Class<? extends Event> subscribeType() {
				return RaftDBErrorEvent.class;
			}
		});
		NotifyCenter.publishEvent(new RaftDBErrorEvent());
		latch1.await();

		result = iconfig7.publishConfig("raft_test_raft_error", "cluster_test_1",
				"this.is.raft_cluster=lessspring_7");
		Assert.assertFalse(result);

		CountDownLatch latch2 = new CountDownLatch(1);
		NotifyCenter.registerSubscribe(new Subscribe<RaftDBErrorRecoverEvent>() {

			@Override
			public void onEvent(RaftDBErrorRecoverEvent event) {
				latch2.countDown();
			}

			@Override
			public Class<? extends Event> subscribeType() {
				return RaftDBErrorRecoverEvent.class;
			}
		});
		NotifyCenter.publishEvent(new RaftDBErrorRecoverEvent());
		latch2.await();

		result = iconfig7.publishConfig("raft_test_raft_error", "cluster_test_1",
				"this.is.raft_cluster=lessspring_7");
		Assert.assertTrue(result);
	}

	@Test
	public void test_f_id_generator_leader_transfer() throws Exception {
		ConfigurableApplicationContext context7 = applications.get("8847");
		ConfigurableApplicationContext context8 = applications.get("8848");
		ConfigurableApplicationContext context9 = applications.get("8849");
		IdGeneratorManager manager7 = context7.getBean(IdGeneratorManager.class);
		IdGeneratorManager manager8 = context8.getBean(IdGeneratorManager.class);
		IdGeneratorManager manager9 = context9.getBean(IdGeneratorManager.class);

		CPProtocol protocol7 = context7.getBean(CPProtocol.class);
		CPProtocol protocol8 = context8.getBean(CPProtocol.class);
		CPProtocol protocol9 = context9.getBean(CPProtocol.class);

		final String configGroup = com.alibaba.nacos.config.server.constant.Constants.CONFIG_MODEL_RAFT_GROUP;
		long preId = -1L;
		long currentId = -1L;

		if (protocol7.isLeader(configGroup)) {
			preId = manager7.nextId(CONFIG_INFO_ID);
		}
		if (protocol8.isLeader(configGroup)) {
			preId = manager8.nextId(CONFIG_INFO_ID);
		}
		if (protocol9.isLeader(configGroup)) {
			preId = manager9.nextId(CONFIG_INFO_ID);
		}

		// transfer leader to ip:8807

		Map<String, String> transfer = new HashMap<>();
		transfer.put(JRaftConstants.TRANSFER_LEADER, InetUtils.getSelfIp() + ":9847");
		RestResult<String> result = protocol7.execute(transfer);
		System.out.println(result);
		Assert.assertTrue(result.ok());

		TimeUnit.SECONDS.sleep(2);

		Assert.assertTrue(protocol7.isLeader(configGroup));
		currentId = manager7.nextId(CONFIG_INFO_ID);
		Assert.assertNotEquals(preId, currentId);
		preId = currentId;

		// transfer leader to ip:8808

		transfer = new HashMap<>();
		transfer.put(JRaftConstants.TRANSFER_LEADER, InetUtils.getSelfIp() + ":9848");
		result = protocol8.execute(transfer);
		System.out.println(result);
		Assert.assertTrue(result.ok());

		TimeUnit.SECONDS.sleep(2);

		Assert.assertTrue(protocol8.isLeader(configGroup));
		currentId = manager8.nextId(CONFIG_INFO_ID);
		Assert.assertNotEquals(preId, currentId);
		preId = currentId;

		// transfer leader to ip:8809

		transfer = new HashMap<>();
		transfer.put(JRaftConstants.TRANSFER_LEADER, InetUtils.getSelfIp() + ":9849");
		result = protocol9.execute(transfer);
		System.out.println(result);
		Assert.assertTrue(result.ok());

		TimeUnit.SECONDS.sleep(2);

		Assert.assertTrue(protocol9.isLeader(configGroup));
		currentId = manager9.nextId(CONFIG_INFO_ID);
		Assert.assertNotEquals(preId, currentId);

	}

	private static void run(final int index, final CountDownLatch latch, final Class<?> cls) {
		Runnable runnable = () -> {
			try {
				ApplicationUtils.setIsStandalone(false);

				final String path = Paths.get(System.getProperty("user.home"), "/nacos-" + index + "/").toString();
				DiskUtils.deleteDirectory(path);

				System.setProperty("nacos.home", path);
				System.out.println("nacos.home is : [" + path + "]");

				Map<String, Object> properties = new HashMap<>();
				properties.put("server.port", "884" + (7 + index));
				properties.put("nacos.home", path);
				properties.put("nacos.logs.path",
						Paths.get(System.getProperty("user.home"), "nacos-" + index, "/logs/").toString());
				properties.put("spring.jmx.enabled", false);
				properties.put("nacos.core.snowflake.worker-id", index + 1);
				MapPropertySource propertySource = new MapPropertySource(
						"nacos_cluster_test", properties);
				ConfigurableEnvironment environment = new StandardServletEnvironment();
				environment.getPropertySources().addFirst(propertySource);
				SpringApplication cluster = new SpringApplicationBuilder(cls).web(
						WebApplicationType.SERVLET).environment(environment)
						.properties(clusterInfo).properties("embeddedStorage=true").build();

				ConfigurableApplicationContext context = cluster.run();

				context.stop();

				DistributedDatabaseOperateImpl operate = context.getBean(DistributedDatabaseOperateImpl.class);
				CPProtocol protocol = context.getBean(CPProtocol.class);

				protocol.protocolMetaData()
						.subscribe(operate.group(), Constants.LEADER_META_DATA,
								(o, arg) -> {
									System.out.println("node : 884" + (7 + index) + "-> select leader is : " + arg);
									if (finished[index].compareAndSet(false, true)) {
										latch.countDown();
									}
								});

				new Thread(() -> {
					try {
						Thread.sleep(5000L);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (finished[index].compareAndSet(false, true)) {
							latch.countDown();
						}
					}
				});

				applications.put(String.valueOf(properties.get("server.port")), context);
			}
			catch (Exception e) {
				e.printStackTrace();
				latch.countDown();
			}
		};

		runnable.run();
	}

}