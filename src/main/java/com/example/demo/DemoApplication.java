package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// 排除資料庫自動設定，只跑 API
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DemoApplication {

    public static void main(String[] args) {
        System.out.println("🚀 啟動 API 模式：正在啟動...");
        // 修正語法錯誤，移除多餘文字
        SpringApplication.run(DemoApplication.class, args);
        System.out.println("✅ 系統已啟動 (API Mode)!");
        System.out.println("👉 請訪問: http://localhost:8080/");
    }
} 