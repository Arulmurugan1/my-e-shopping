package com.myeshopping.authservice.repository;

import com.myeshopping.authservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsernameIgnoreCase(String username);
    Optional<UserEntity> findByMobileNumber(String mobileNumber);
    boolean existsByEmail(String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByMobileNumber(String mobileNumber);
}
