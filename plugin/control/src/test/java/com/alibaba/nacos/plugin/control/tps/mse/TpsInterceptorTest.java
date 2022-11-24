package com.alibaba.nacos.plugin.control.tps.mse;

import com.alibaba.nacos.plugin.control.tps.MonitorType;
import com.alibaba.nacos.plugin.control.tps.TpsControlManager;
import com.alibaba.nacos.plugin.control.tps.key.MonitorKey;
import com.alibaba.nacos.plugin.control.tps.response.TpsCheckResponse;
import com.alibaba.nacos.plugin.control.tps.response.TpsResultCode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.plugin.control.tps.mse.MseRuleDetail.MODEL_FUZZY;

public class TpsInterceptorTest {
    
    TpsControlManager tpsControlManager = new TpsControlManager();
    
    String pointName = "interceptortest";
    
    @Before
    public void setUp() {
        //1.register point
        tpsControlManager.registerTpsPoint(pointName);
        Assert.assertTrue(tpsControlManager.getPoints().containsKey(pointName));
        
    }
    
    /**
     * testPassByMonitor.
     */
    @Test
    public void testPassByMonitor() {
        //1.register rule
        MseRuleDetail ruleDetail = new MseRuleDetail();
        ruleDetail.setMaxCount(10000);
        ruleDetail.setPeriod(TimeUnit.SECONDS);
        ruleDetail.setMonitorType(MonitorType.MONITOR.getType());
        ruleDetail.setModel(MODEL_FUZZY);
        ruleDetail.setPattern("test:prefix*");
        MseTpsControlRule tpsControlRule = new MseTpsControlRule();
        tpsControlRule.setPointName(pointName);
        tpsControlRule.setPointRule(ruleDetail);
        
        MseRuleDetail ruleDetailMonitor = new MseRuleDetail();
        ruleDetailMonitor.setMaxCount(20);
        ruleDetailMonitor.setPeriod(TimeUnit.SECONDS);
        ruleDetailMonitor.setMonitorType(MonitorType.INTERCEPT.getType());
        ruleDetailMonitor.setModel(MODEL_FUZZY);
        ruleDetailMonitor.setPattern("test:prefixmonitor*");
        tpsControlRule.getMonitorKeyRule().put("monitorkey", ruleDetailMonitor);
        
        tpsControlManager.applyTpsRule(pointName, tpsControlRule);
        Assert.assertTrue(tpsControlManager.getRules().containsKey(pointName));
        Assert.assertTrue(((MseTpsControlRule) tpsControlManager.getRules().get(pointName)).getMonitorKeyRule()
                .containsKey("monitorkey"));
        
        //3.apply tps
        MseTpsCheckRequest tpsCheckRequest = new MseTpsCheckRequest();
        tpsCheckRequest.setCount(2);
        tpsCheckRequest.setPointName(pointName);
        List<MonitorKey> monitorKeyList = new ArrayList<>();
        monitorKeyList.add(new MonitorKey("prefixmonitor123") {
            @Override
            public String getType() {
                return "test";
            }
        });
        
        tpsCheckRequest.setMonitorKeys(monitorKeyList);
        for (int i = 0; i < 10; i++) {
            TpsCheckResponse check = tpsControlManager.check(tpsCheckRequest);
            Assert.assertTrue(check.isSuccess());
        }
        
        TpsCheckResponse check = tpsControlManager.check(tpsCheckRequest);
        Assert.assertFalse(check.isSuccess());
        
        tpsCheckRequest.setClientIp("127.0.0.10");
        TpsCheckResponse checkInterceptor = tpsControlManager.check(tpsCheckRequest);
        Assert.assertEquals(TpsResultCode.DENY_BY_PATTERN, checkInterceptor.getCode());
        
        Assert.assertFalse(checkInterceptor.isSuccess());
        
    }
}
