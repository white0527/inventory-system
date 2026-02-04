package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        // 這是新的啟動訊息，代表我們改用設定檔了
        System.out.println("🚀 祥易系統啟動中... (正在讀取 application.properties)");
        
        SpringApplication.run(DemoApplication.class, args);
        
        System.out.println("✅ 系統已啟動成功!");
        System.out.println("👉 請開啟網頁: http://localhost:8080/");
    }
}