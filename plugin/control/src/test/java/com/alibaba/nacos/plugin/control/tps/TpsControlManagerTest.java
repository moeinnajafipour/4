package com.alibaba.nacos.plugin.control.tps;

import com.alibaba.nacos.plugin.control.configs.ControlConfigs;
import com.alibaba.nacos.plugin.control.tps.key.MonitorKey;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;
import com.alibaba.nacos.plugin.control.tps.response.TpsCheckResponse;
import com.alibaba.nacos.plugin.control.tps.rule.RuleDetail;
import com.alibaba.nacos.plugin.control.tps.rule.TpsControlRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.plugin.control.tps.rule.RuleDetail.MODEL_FUZZY;

@RunWith(MockitoJUnitRunner.class)
public class TpsControlManagerTest {
    
    TpsControlManager tpsControlManager = new TpsControlManager();
    
    String pointName = "TEST_POINT_NAME" + System.currentTimeMillis();
    
    static {
        ControlConfigs.setInstance(new ControlConfigs());
        
    }
    
    @Before
    public void setUp() {
        //1.register point
        tpsControlManager.registerTpsPoint(pointName);
        Assert.assertTrue(tpsControlManager.getPoints().containsKey(pointName));
        
    }
    
    /**
     * test denied by monitor key rules.
     */
    @Test
    public void testPatternDeny() {
        
        tpsControlManager.applyTpsRule(pointName, null);
        //1.register rule
        RuleDetail ruleDetail = new RuleDetail();
        ruleDetail.setMaxCount(10000);
        ruleDetail.setPeriod(TimeUnit.SECONDS);
        ruleDetail.setMonitorType(MonitorType.MONITOR.getType());
        ruleDetail.setModel(MODEL_FUZZY);
        ruleDetail.setPattern("test:prefix*");
        TpsControlRule tpsControlRule = new TpsControlRule();
        tpsControlRule.setPointName(pointName);
        tpsControlRule.setPointRule(ruleDetail);
        
        RuleDetail ruleDetailMonitor = new RuleDetail();
        ruleDetailMonitor.setMaxCount(20);
        ruleDetailMonitor.setPeriod(TimeUnit.SECONDS);
        ruleDetailMonitor.setMonitorType(MonitorType.INTERCEPT.getType());
        ruleDetailMonitor.setModel(MODEL_FUZZY);
        ruleDetailMonitor.setPattern("test:prefixmonitor*");
        tpsControlRule.getMonitorKeyRule().put("monitorkey", ruleDetailMonitor);
        
        tpsControlManager.applyTpsRule(pointName, tpsControlRule);
        Assert.assertTrue(tpsControlManager.getRules().containsKey(pointName));
        
        //3.apply tps
        TpsCheckRequest tpsCheckRequest = new TpsCheckRequest();
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
    }
}
