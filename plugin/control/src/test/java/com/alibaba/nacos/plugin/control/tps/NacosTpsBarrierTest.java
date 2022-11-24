package com.alibaba.nacos.plugin.control.tps;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.plugin.control.tps.nacos.NacosTpsBarrier;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;
import com.alibaba.nacos.plugin.control.tps.response.TpsCheckResponse;
import com.alibaba.nacos.plugin.control.tps.rule.RuleDetail;
import com.alibaba.nacos.plugin.control.tps.rule.TpsControlRule;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class NacosTpsBarrierTest {
    
    @Test
    public void testNormalPointPassAndMonitorKeyDeny() {
        String testTpsBarrier = "test_barrier";
        
        TpsControlRule tpsControlRule = new TpsControlRule();
        tpsControlRule.setPointName(testTpsBarrier);
        
        RuleDetail ruleDetail = new RuleDetail();
        ruleDetail.setMaxCount(6);
        ruleDetail.setMonitorType(MonitorType.INTERCEPT.getType());
        ruleDetail.setPeriod(TimeUnit.SECONDS);
        tpsControlRule.setPointRule(ruleDetail);
        
        RuleDetail monitorRuleDetail = new RuleDetail();
        monitorRuleDetail.setMaxCount(5);
        monitorRuleDetail.setMonitorType(MonitorType.INTERCEPT.getType());
        monitorRuleDetail.setPeriod(TimeUnit.SECONDS);
   
        TpsBarrier tpsBarrier = new NacosTpsBarrier(testTpsBarrier);
        tpsBarrier.applyRule(tpsControlRule);
        
        //test point keys
        long timeMillis = System.currentTimeMillis();
        TpsCheckRequest tpsCheckRequest = new TpsCheckRequest();
        tpsCheckRequest.setTimestamp(timeMillis);
        
        for (int i = 0; i < 5; i++) {
            TpsCheckResponse tpsCheckResponse = tpsBarrier.applyTps(tpsCheckRequest);
            Assert.assertTrue(tpsCheckResponse.isSuccess());
        }
        TpsCheckResponse tpsCheckResponse = tpsBarrier.applyTps(tpsCheckRequest);
        Assert.assertFalse(tpsCheckResponse.isSuccess());
        System.out.println(tpsCheckResponse.getMessage());
        
    }
    
    @Test
    public void testNormalPointDenyAndMonitorKeyPass() {
        String testTpsBarrier = "test_barrier";
        
        TpsControlRule tpsControlRule = new TpsControlRule();
        tpsControlRule.setPointName(testTpsBarrier);
        
        RuleDetail ruleDetail = new RuleDetail();
        ruleDetail.setMaxCount(5);
        ruleDetail.setMonitorType(MonitorType.INTERCEPT.getType());
        ruleDetail.setPeriod(TimeUnit.SECONDS);
        tpsControlRule.setPointRule(ruleDetail);
        
        RuleDetail monitorRuleDetail = new RuleDetail();
        monitorRuleDetail.setMaxCount(6);
        monitorRuleDetail.setMonitorType(MonitorType.INTERCEPT.getType());
        monitorRuleDetail.setPeriod(TimeUnit.SECONDS);
        
        TpsBarrier tpsBarrier = new NacosTpsBarrier(testTpsBarrier);
        tpsBarrier.applyRule(tpsControlRule);
        System.out.println(JacksonUtils.toJson(tpsControlRule));
        //test point keys
        long timeMillis = System.currentTimeMillis();
        TpsCheckRequest tpsCheckRequest = new TpsCheckRequest();
        
        tpsCheckRequest.setTimestamp(timeMillis);
        
        for (int i = 0; i < 5; i++) {
            TpsCheckResponse tpsCheckResponse = tpsBarrier.applyTps(tpsCheckRequest);
            Assert.assertTrue(tpsCheckResponse.isSuccess());
        }
        TpsCheckResponse tpsCheckResponse = tpsBarrier.applyTps(tpsCheckRequest);
        Assert.assertFalse(tpsCheckResponse.isSuccess());
        System.out.println(tpsCheckResponse.getMessage());
        
    }
    
    @Test
    public void testNormalConnectionAndClientIpMonitor() {
        String testTpsBarrier = "test_barrier";
        
        TpsControlRule tpsControlRule = new TpsControlRule();
        tpsControlRule.setPointName(testTpsBarrier);
        
        RuleDetail ruleDetail = new RuleDetail();
        ruleDetail.setMaxCount(6);
        ruleDetail.setMonitorType(MonitorType.INTERCEPT.getType());
        ruleDetail.setPeriod(TimeUnit.SECONDS);
        tpsControlRule.setPointRule(ruleDetail);
        
        RuleDetail connectionIdRuleDetail = new RuleDetail();
        connectionIdRuleDetail.setMaxCount(5);
        connectionIdRuleDetail.setMonitorType(MonitorType.INTERCEPT.getType());
        connectionIdRuleDetail.setPeriod(TimeUnit.SECONDS);
       
        RuleDetail clientIpRuleDetail = new RuleDetail();
        clientIpRuleDetail.setMaxCount(5);
        clientIpRuleDetail.setMonitorType(MonitorType.INTERCEPT.getType());
        clientIpRuleDetail.setPeriod(TimeUnit.SECONDS);
       
        TpsBarrier tpsBarrier = new NacosTpsBarrier(testTpsBarrier);
        tpsBarrier.applyRule(tpsControlRule);
        
        //test point keys
        long timeMillis = System.currentTimeMillis();
        TpsCheckRequest tpsCheckRequest = new TpsCheckRequest();
        tpsCheckRequest.setTimestamp(timeMillis);
        
        for (int i = 0; i < 5; i++) {
            TpsCheckResponse tpsCheckResponse = tpsBarrier.applyTps(tpsCheckRequest);
            Assert.assertTrue(tpsCheckResponse.isSuccess());
        }
        TpsCheckResponse tpsCheckResponse = tpsBarrier.applyTps(tpsCheckRequest);
        Assert.assertFalse(tpsCheckResponse.isSuccess());
        System.out.println(tpsCheckResponse.getMessage());
        
    }
}
