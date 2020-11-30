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

package com.alibaba.nacos.core.cluster;

import com.alibaba.nacos.sys.env.EnvUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockServletContext;

import java.net.ConnectException;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class MemberUtilTest {
    
    private static final String IP = "1.1.1.1";
    
    private static final int PORT = 8848;
    
    private ConfigurableEnvironment environment;
    
    private Member originalMember;
    
    private ServerMemberManager memberManager;
    
    @Before
    public void setUp() throws Exception {
        environment = new MockEnvironment();
        EnvUtil.setEnvironment(environment);
        memberManager = new ServerMemberManager(new MockServletContext());
        originalMember = buildMember();
    }
    
    private Member buildMember() {
        return Member.builder().ip(IP).port(PORT).state(NodeState.UP).build();
    }
    
    @Test
    public void testIsBasicInfoChangedNoChangeWithoutExtendInfo() {
        Member newMember = buildMember();
        assertFalse(MemberUtil.isBasicInfoChanged(newMember, originalMember));
    }
    
    @Test
    public void testIsBasicInfoChangedNoChangeWithExtendInfo() {
        Member newMember = buildMember();
        newMember.setExtendVal("test", "test");
        assertFalse(MemberUtil.isBasicInfoChanged(newMember, originalMember));
    }
    
    @Test
    public void testIsBasicInfoChangedForIp() {
        Member newMember = buildMember();
        newMember.setIp("1.1.1.2");
        assertTrue(MemberUtil.isBasicInfoChanged(newMember, originalMember));
    }
    
    @Test
    public void testIsBasicInfoChangedForPort() {
        Member newMember = buildMember();
        newMember.setPort(PORT + 1);
        assertTrue(MemberUtil.isBasicInfoChanged(newMember, originalMember));
    }
    
    @Test
    public void testIsBasicInfoChangedForAddress() {
        Member newMember = buildMember();
        newMember.setAddress("test");
        assertTrue(MemberUtil.isBasicInfoChanged(newMember, originalMember));
    }
    
    @Test
    public void testIsBasicInfoChangedForStatus() {
        Member newMember = buildMember();
        newMember.setState(NodeState.DOWN);
        assertTrue(MemberUtil.isBasicInfoChanged(newMember, originalMember));
    }
    
    @Test
    public void testIsBasicInfoChangedForMoreBasicExtendInfo() {
        Member newMember = buildMember();
        newMember.setExtendVal(MemberMetaDataConstants.VERSION, "TEST");
        assertTrue(MemberUtil.isBasicInfoChanged(newMember, originalMember));
    }
    
    @Test
    public void testIsBasicInfoChangedForChangedBasicExtendInfo() {
        Member newMember = buildMember();
        newMember.setExtendVal(MemberMetaDataConstants.WEIGHT, "100");
        assertTrue(MemberUtil.isBasicInfoChanged(newMember, originalMember));
    }
    
    @Test
    public void testMemberOnFailWhenReachMaxFailAccessCnt() {
        final Member remote = buildMember();
        memberManager.memberJoin(Collections.singletonList(remote));
        
        remote.setFailAccessCnt(2);
        MemberUtil.onFail(remote);
        
        final Member search1 = memberManager.find(remote.getAddress());
        Assert.assertEquals(3, search1.getFailAccessCnt());
        Assert.assertEquals(NodeState.SUSPICIOUS, search1.getState());
    
        MemberUtil.onFail(remote);
    
        final Member search2 = memberManager.find(remote.getAddress());
        Assert.assertEquals(4, search2.getFailAccessCnt());
        Assert.assertEquals(NodeState.DOWN, search2.getState());
        
        MemberUtil.onSuccess(remote);
        final Member search3 = memberManager.find(remote.getAddress());
        Assert.assertEquals(0, search3.getFailAccessCnt());
        Assert.assertEquals(NodeState.UP, search3.getState());
    }
    
    @Test
    public void testMemberOnFailWhenConnectRefused() {
        final Member remote = buildMember();
        memberManager.memberJoin(Collections.singletonList(remote));
        
        remote.setFailAccessCnt(1);
        MemberUtil.onFail(remote, new ConnectException(MemberUtil.TARGET_MEMBER_CONNECT_REFUSE_ERRMSG));
        
        final Member search1 = memberManager.find(remote.getAddress());
        Assert.assertEquals(2, search1.getFailAccessCnt());
        Assert.assertEquals(NodeState.DOWN, search1.getState());
    
        MemberUtil.onSuccess(remote);
        final Member search2 = memberManager.find(remote.getAddress());
        Assert.assertEquals(0, search2.getFailAccessCnt());
        Assert.assertEquals(NodeState.UP, search2.getState());
    }
}
