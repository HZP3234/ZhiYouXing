package com.zhiyouxing.hotel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 接入 zhiyouxing 库：common 模块的 Common/Config/Token/Storeup Service 依赖其中的
 * config / token / store_up 表，因此这里必须扫描 com.zhiyouxing.common.dao 并保留数据源自动装配。
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.zhiyouxing")
@MapperScan({"com.zhiyouxing.hotel.dao", "com.zhiyouxing.common.dao"})
@SpringBootApplication(scanBasePackages = "com.zhiyouxing")
public class HotelApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelApplication.class, args);
    }
}
