package com.zhiyouxing.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 接入 zhiyouxing 库：common 模块的 Token/Storeup Service 依赖其中的
 * token / store_up 表，因此这里必须扫描 com.zhiyouxing.common.dao 并保留数据源自动装配。
 *
 * 另外本服务的 ConsumptionDao 会跨表读三张订单表（ticket_order / hotel_reservation /
 * group_tour）—— 它们由 attraction / hotel / travel 三个服务写，但同在一个库里。
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.zhiyouxing")
@MapperScan({"com.zhiyouxing.user.dao", "com.zhiyouxing.common.dao"})
@SpringBootApplication(scanBasePackages = "com.zhiyouxing")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
