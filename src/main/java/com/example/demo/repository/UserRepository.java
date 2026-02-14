package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // 透過帳號尋找使用者 (登入驗證用)
    Optional<User> findByUsername(String username);
}