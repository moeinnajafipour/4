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
package com.alibaba.nacos.naming.consistency.ephemeral.partition;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Stores some configurations for Partition protocol
 *
 * @author nkorange
 * @since 1.0.0
 */
@Component
public class PartitionConfig {

    @Value("${nacos.naming.partition.taskDispatchPeriod}")
    private int taskDispatchPeriod = 2000;

    @Value("${nacos.naming.partition.batchSyncKeyCount}")
    private int batchSyncKeyCount = 1000;

    public int getTaskDispatchPeriod() {
        return taskDispatchPeriod;
    }

    public int getBatchSyncKeyCount() {
        return batchSyncKeyCount;
    }
}
