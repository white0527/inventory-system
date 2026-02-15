package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // 必須引入非同步支援

@SpringBootApplication
@EnableAsync // 啟動非同步功能，讓三萬筆資料可以在背景慢慢跑
public class DemoApplication {

    public static void main(String[] args) {
        // 這是新的啟動訊息，代表我們改用設定檔了
        System.out.println("🚀 祥易系統啟動中... (正在讀取 application.properties)");
        
        SpringApplication.run(DemoApplication.class, args);
        
        System.out.println("✅ 系統已啟動成功!");
        System.out.println("👉 如果是本機測試請開啟: http://localhost:8080/");
        System.out.println("👉 如果是雲端執行請開啟您的 Render 網址");
    }
}