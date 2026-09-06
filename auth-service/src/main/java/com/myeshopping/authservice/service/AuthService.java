package com.myeshopping.authservice.service;

import com.myeshopping.authservice.dto.AuthResponse;
import com.myeshopping.authservice.dto.LoginRequest;
import com.myeshopping.authservice.dto.RegisterRequest;
import com.myeshopping.authservice.entity.Gender;
import com.myeshopping.authservice.entity.Role;
import com.myeshopping.authservice.entity.UserEntity;
import com.myeshopping.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email already registered");
        }

        UserEntity user = UserEntity.builder()
                .email(request.getEmail().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .dateOfBirth(request.getDateOfBirth())
                .gender(parseGender(request.getGender()))
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        UserEntity saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getEmail(), saved.getRole().name(), saved.getId().toString());
        String refreshToken = jwtService.generateRefreshToken(saved.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .userId(saved.getId().toString())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isActive()) {
            throw new IllegalStateException("Inactive user");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId().toString());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId().toString())
                .build();
    }

    private Gender parseGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return Gender.OTHER;
        }
        return Gender.valueOf(gender.trim().toUpperCase());
    }
}
