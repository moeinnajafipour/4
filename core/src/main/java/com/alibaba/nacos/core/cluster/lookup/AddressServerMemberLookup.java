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

package com.alibaba.nacos.core.cluster.lookup;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.http.HttpClientManager;
import com.alibaba.nacos.common.http.NSyncHttpClient;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.common.model.RestResult;
import com.alibaba.nacos.core.cluster.MemberUtils;
import com.alibaba.nacos.core.utils.ApplicationUtils;
import com.alibaba.nacos.core.utils.GenericType;
import com.alibaba.nacos.core.utils.GlobalExecutor;
import com.alibaba.nacos.core.utils.Loggers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Cluster member addressing mode for the address server
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class AddressServerMemberLookup extends AbstractMemberLookup {

	private final GenericType<RestResult<String>> genericType = new GenericType<RestResult<String>>() {
	};

	public String domainName;
	public String addressPort;
	public String addressUrl;
	public String envIdUrl;
	public String addressServerUrl;
	private volatile boolean isAddressServerHealth = true;
	private int addressServerFailCount = 0;
	private int maxFailCount = 12;
	private NSyncHttpClient syncHttpClient = HttpClientManager.getShareSyncHttpClient();
	private volatile boolean shutdown = false;

	@Override
	public void start() throws NacosException {
		if (start.compareAndSet(false, true)) {
			this.maxFailCount = Integer.parseInt(ApplicationUtils.getProperty("maxHealthCheckFailCount", "12"));
			initAddressSys();
			run();
		}
	}

	private void initAddressSys() {
		String envDomainName = System.getenv("address_server_domain");
		if (StringUtils.isBlank(envDomainName)) {
			domainName = System.getProperty("address.server.domain", "jmenv.tbsite.net");
		} else {
			domainName = envDomainName;
		}
		String envAddressPort = System.getenv("address_server_port");
		if (StringUtils.isBlank(envAddressPort)) {
			addressPort = System.getProperty("address.server.port", "8080");
		} else {
			addressPort = envAddressPort;
		}
		addressUrl = System.getProperty("address.server.url",
				ApplicationUtils.getContextPath() + "/" + "serverlist");
		addressServerUrl = "http://" + domainName + ":" + addressPort + addressUrl;
		envIdUrl = "http://" + domainName + ":" + addressPort + "/env";

		Loggers.CORE.info("ServerListService address-server port:" + addressPort);
		Loggers.CORE.info("ADDRESS_SERVER_URL:" + addressServerUrl);
	}

	@SuppressWarnings("PMD.UndefineMagicConstantRule")
	private void run() throws NacosException {
		// With the address server, you need to perform a synchronous member node pull at startup
		// Repeat three times, successfully jump out
		boolean success = false;
		Throwable ex = null;
		int maxRetry = ApplicationUtils.getProperty("nacos.core.address-server.retry", Integer.class, 5);
		for (int i = 0; i < maxRetry; i ++) {
			try {
				syncFromAddressUrl();
				success = true;
				break;
			} catch (Throwable e) {
				ex = e;
				Loggers.CLUSTER.error("[serverlist] exception, error : {}", ex);
			}
		}
		if (!success) {
			throw new NacosException(NacosException.SERVER_ERROR, ex);
		}

		GlobalExecutor.scheduleByCommon(new AddressServerSyncTask(), 5_000L);
	}

	@Override
	public void destroy() throws NacosException {
		shutdown = true;
	}

	@Override
	public Map<String, Object> info() {
		Map<String, Object> info = new HashMap<>(4);
		info.put("addressServerHealth", isAddressServerHealth);
		info.put("addressServerUrl", addressServerUrl);
		info.put("envIdUrl", envIdUrl);
		info.put("addressServerFailCount", addressServerFailCount);
		return info;
	}

	class AddressServerSyncTask implements Runnable {

		@Override
		public void run() {
			if (shutdown) {
				return;
			}
			try {
				syncFromAddressUrl();
			}
			catch (Throwable ex) {
				addressServerFailCount++;
				if (addressServerFailCount >= maxFailCount) {
					isAddressServerHealth = false;
				}
				Loggers.CLUSTER.error("[serverlist] exception, error : {}", ex);
			} finally {
				GlobalExecutor.scheduleByCommon(this, 5_000L);
			}
		}
	}

	private void syncFromAddressUrl() throws Exception {
		RestResult<String> result = syncHttpClient
				.get(addressServerUrl, Header.EMPTY, Query.EMPTY,
						genericType.getType());
		if (HttpStatus.OK.value() == result.getCode()) {
			isAddressServerHealth = true;
			Reader reader = new StringReader(result.getData());
			try {
				afterLookup(MemberUtils.readServerConf(ApplicationUtils.analyzeClusterConf(reader)));
			}
			catch (Throwable e) {
				Loggers.CLUSTER
						.error("[serverlist] exception for analyzeClusterConf, error : {}",
								e);
			}
			addressServerFailCount = 0;
			isAddressServerHealth = false;
		}
		else {
			addressServerFailCount++;
			if (addressServerFailCount >= maxFailCount) {
				isAddressServerHealth = false;
			}
			Loggers.CLUSTER.error("[serverlist] failed to get serverlist, error code {}",
					result.getCode());
		}
	}
}
