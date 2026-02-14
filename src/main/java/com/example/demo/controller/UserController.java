package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 系統啟動時，檢查並自動建立預設老闆帳號
    @PostConstruct
    public void initDefaultAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("8888");
            admin.setRole("boss");
            userRepository.save(admin);
        }
    }

    // 1. 登入驗證 API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            // 密碼正確，回傳權限身分
            return ResponseEntity.ok(Map.of("success", true, "role", userOpt.get().getRole()));
        }
        return ResponseEntity.status(401).body(Map.of("success", false, "message", "帳號或密碼錯誤"));
    }

    // 2. 取得所有員工帳號清單 (老闆管理用)
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 3. 新增員工帳號
    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody User newUser) {
        if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "帳號已被使用"));
        }
        newUser.setRole("staff"); // 從介面新增的強制為員工
        userRepository.save(newUser);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // 4. 刪除帳號
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}