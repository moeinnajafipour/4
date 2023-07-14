/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.lock.core.service;

import com.alibaba.nacos.api.lock.model.LockInfo;

/**
 * lock operator service.
 * @author 985492783@qq.com
 * @date 2023/6/28 2:38
 */
public interface LockOperationService {

    /**
     * get lock operator.
     * @param lockInfo lockInfo
     * @param connectionId connectionId
     * @return boolean
     */
    Boolean lock(LockInfo lockInfo, String connectionId);
    
    /**
     * unLock.
     * @param lockInfo lockInfo
     * @return Boolean
     */
    Boolean unLock(LockInfo lockInfo);
    
}
