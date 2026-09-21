package com.zhiyouxing.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Agent 微服务：本模块不访问数据库，排除数据源自动装配以保证可独立启动。
 */
@EnableDiscoveryClient
@SpringBootApplication(
        scanBasePackages = {"com.zhiyouxing.agent", "com.zhiyouxing.common.exception"},
        exclude = DataSourceAutoConfiguration.class)
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
