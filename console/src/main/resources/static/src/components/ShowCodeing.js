import React from 'react'; 
import { Dialog, Loading, Tab } from '@alifd/next';
const TabPane = Tab.Item; 

/*****************************此行为标记行, 请勿删和修改此行, 文件和组件依赖请写在此行上面, 主体代码请写在此行下面的class中*****************************/
class ShowCodeing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            dialogvisible: false,
            loading: false
        };
        this.defaultCode = ``;
        this.nodejsCode = `TODO`;
        this.cppCode = `TODO`;
        this.shellCode = `TODO`;
        this.pythonCode = `TODO`;
        this.record = {};
        this.sprigboot_code = `// Refer to document:  https://help.aliyun.com/document_detail/60369.html
package com.alibaba.cloud.acm.sample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created on 01/10/2017.
 * Assume the configuration content is as follows
 * user.id = 0
 * user.name = juven.xxxb
 * user.age = 18
 * @author juven.xuxb
 */
@SpringBootApplication
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }
}

@Component
class SampleRunner implements ApplicationRunner {

    @Value("\${user.id}")
    String userId;

    @Value("\${user.name}")
    String userName;

    @Value("\${user.age}")
    int userAge;

    public void run(ApplicationArguments args) throws Exception {
        System.out.println(userId);
        System.out.println(userName);
        System.out.println(userAge);
    }
}

@RestController
@RequestMapping("/sample")
@RefreshScope
class SampleController {

    @Value("\${user.name}")
    String userName;

    @RequestMapping("/acm")
    public String simple() {
        return "Hello Spring Cloud ACM!" + "Hello " + userName + "!";
    }
}`;
    }

    componentDidMount() {}
    openLoading() {
        this.setState({
            loading: true
        });
    }
    closeLoading() {
        this.setState({
            loading: false
        });
    }

    getData() {
        let namespace = getParams('namespace'); //获取ak,sk
        let obj = {
            group: this.record.group || '',
            dataId: this.record.dataId || '',
            namespace: namespace,
            inEdas: window.globalConfig.isParentEdas()
        };
        this.defaultCode = this.getJavaCode(obj);
        this.createCodeMirror('text/x-java', this.defaultCode);
        this.nodejsCode = this.getNodejsCode(obj);
        this.cppCode = this.getCppCode(obj);
        this.shellCode = this.getShellCode(obj);
        this.pythonCode = this.getPythonCode(obj);
    }

    getJavaCode(data) {
        return `/*
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
package com.alibaba.nacos.example;

import java.util.Properties;
import java.util.concurrent.Executor;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;

/**
 * Config service example
 * 
 * @author Nacos
 *
 */
public class ConfigExample {

	public static void main(String[] args) throws NacosException, InterruptedException {
		String serverAddr = "localhost";
		String dataId = "${data.dataId}";
		String group = "${data.group}";
		Properties properties = new Properties();
		properties.put("serverAddr", serverAddr);
		ConfigService configService = NacosFactory.createConfigService(properties);
		String content = configService.getConfig(dataId, group, 5000);
		System.out.println(content);
		configService.addListener(dataId, group, new Listener() {
			@Override
			public void receiveConfigInfo(String configInfo) {
				System.out.println("recieve:" + configInfo);
			}

			@Override
			public Executor getExecutor() {
				return null;
			}
		});
		
		boolean isPublishOk = configService.publishConfig(dataId, group, "content");
		System.out.println(isPublishOk);
		
		Thread.sleep(3000);
		content = configService.getConfig(dataId, group, 5000);
		System.out.println(content);

		boolean isRemoveOk = configService.removeConfig(dataId, group);
		System.out.println(isRemoveOk);
		Thread.sleep(3000);

		content = configService.getConfig(dataId, group, 5000);
		System.out.println(content);
		Thread.sleep(300000);

	}
}
`;
    }

    getNodejsCode(data) {
       return `TODO`;
    }

    getCppCode(data) {
        return `TODO`;
    }

    getShellCode(data) {
        return `TODO`;
    }

    getPythonCode(data) {
    	return `TODO`;
    }

    openDialog(record) {
        this.setState({
            dialogvisible: true
        });
        this.record = record;
        setTimeout(() => {

            this.getData(); //获取数据
        });
    }

    closeDialog() {
        this.setState({
            dialogvisible: false
        });
    }

    createCodeMirror(mode, value) {
        let commontarget = this.refs['codepreview'];
        if (commontarget) {
            commontarget.innerHTML = '';
            this.cm = CodeMirror(commontarget, {
                value: value,
                mode: mode,
                height: 400,
                width: 500,
                lineNumbers: true,
                theme: 'xq-light',
                lint: true,
                tabMode: "indent",
                autoMatchParens: true,
                textWrapping: true,
                gutters: ["CodeMirror-lint-markers"],
                extraKeys: {
                    "F1": function (cm) {
                        cm.setOption("fullScreen", !cm.getOption("fullScreen"));
                    },
                    "Esc": function (cm) {
                        if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
                    }
                }
            });
        }

        //this.cm.setSize(window.innerWidth*0.8-10,400);//设置宽高
    }
    changeTab(key, code) {
        setTimeout(() => {
            this[key] = true;

            this.createCodeMirror('text/javascript', code);
        });
    }
    render() {
        const footer = <div></div>;
        return (
            <div>
                <Dialog title={aliwareIntl.get('com.alibaba.nacos.component.ShowCodeing.Sample_code')} style={{ width: '80%' }} visible={this.state.dialogvisible} footer={footer} onClose={this.closeDialog.bind(this)} language={aliwareIntl.currentLanguageCode}>
                    <div style={{ height: 500 }}>
                        <Loading tip={aliwareIntl.get('com.alibaba.nacos.component.ShowCodeing.loading')} style={{ width: '100%' }} visible={this.state.loading}>
                            <Tab shape={'text'} style={{ height: 40, paddingBottom: 10 }}>
                                <TabPane title={'Java'} key={1} onClick={this.changeTab.bind(this, 'commoneditor1', this.defaultCode)}>

                                </TabPane>
                                <TabPane title={'Spring Boot'} key={2} onClick={this.changeTab.bind(this, 'commoneditor2', this.sprigboot_code)}>

                                </TabPane>
                                <TabPane title={'Node.js'} key={3} onClick={this.changeTab.bind(this, 'commoneditor3', this.nodejsCode)}>

                                </TabPane>
                                <TabPane title={'C++'} key={4} onClick={this.changeTab.bind(this, 'commoneditor4', this.cppCode)}>

                                </TabPane>

                                <TabPane title={'Shell'} key={5} onClick={this.changeTab.bind(this, 'commoneditor5', this.shellCode)}>

                                </TabPane>
                                <TabPane title={'Python'} key={6} onClick={this.changeTab.bind(this, 'commoneditor6', this.pythonCode)}>

                                </TabPane>
                                {}
                            </Tab>
                            <div ref={'codepreview'}></div>
                        </Loading>
                    </div>
                </Dialog>

            </div>
        );
    }
}
/*****************************此行为标记行, 请勿删和修改此行, 主体代码请写在此行上面的class中, 组件导出语句及其他信息请写在此行下面*****************************/
export default ShowCodeing;