package com.smartparking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartParkingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartParkingApplication.class, args);
        System.out.println("========================================");
        System.out.println("  智能停车云平台后端服务启动成功！");
        System.out.println("  中文接口文档: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
