package com.zhiyouxing.food;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.zhiyouxing")
@MapperScan({"com.zhiyouxing.food.dao", "com.zhiyouxing.common.dao"})
@SpringBootApplication(scanBasePackages = "com.zhiyouxing")
public class FoodApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodApplication.class, args);
    }
}
