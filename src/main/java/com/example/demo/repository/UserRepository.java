package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 必須新增這行，才能根據輸入的帳號去 Supabase 撈資料
    Optional<User> findByUsername(String username);
}