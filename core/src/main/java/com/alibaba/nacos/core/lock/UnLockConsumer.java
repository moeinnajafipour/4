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

package com.alibaba.nacos.core.lock;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.model.ResResult;
import com.alibaba.nacos.core.distributed.Datum;
import com.alibaba.nacos.core.distributed.LogConsumer;
import com.alibaba.nacos.core.utils.ResResultUtils;

import java.util.Map;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
class UnLockConsumer implements LogConsumer {

    private LockManager lockManager;

    public UnLockConsumer(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    @Override
    public ResResult<Boolean> onAccept(Datum data) {
        final String key = data.getKey();
        Map<String, LockManager.LockAttempt> attemptMap = lockManager.getLockAttempts();
        if (attemptMap.containsKey(key)) {
            return ResResultUtils.failed("The resource is not locked");
        }
        final LockEntry entry = JSON.parseObject(data.getData(), LockEntry.class);
        LockManager.LockAttempt attempt = attemptMap.get(key);
        final long version = entry.getVersion();
        if (attempt.getVersion() == version) {
            attempt.setExpireTimeMs(System.currentTimeMillis() - 1);
        }
        return ResResultUtils.failed("Without this version, the lock cannot be released");
    }

    @Override
    public String operation() {
        return LockOperation.UN_LOCK.getOperation();
    }
}
