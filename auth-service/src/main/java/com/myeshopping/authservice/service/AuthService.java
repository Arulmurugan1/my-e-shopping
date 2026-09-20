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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String MOBILE_DIGITS = "^\\+?[0-9]{7,15}$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim();
        String username = request.getUsername().trim();
        String mobile = normalizeMobile(request.getMobileNumber());
        String dateOfBirth = validateDateOfBirth(request.getDateOfBirth());

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByMobileNumber(mobile)) {
            throw new IllegalArgumentException("Mobile number already registered");
        }

        UserEntity user = UserEntity.builder()
                .email(email)
                .username(username)
                .mobileNumber(mobile)
                .password(passwordEncoder.encode(request.getPassword()))
                .dateOfBirth(dateOfBirth)
                .gender(parseGender(request.getGender()))
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        return toResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        UserEntity user = resolve(request.getIdentifier().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isActive()) {
            throw new DisabledException("Account is deactivated");
        }

        // The JWT subject and the security context are keyed by email, so authenticate against it.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword()));

        return toResponse(user);
    }

    private Optional<UserEntity> resolve(String identifier) {
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier);
        }
        String digits = identifier.replaceAll("[\\s()\\-]", "");
        if (digits.matches(MOBILE_DIGITS)) {
            Optional<UserEntity> byMobile = userRepository.findByMobileNumber(digits);
            if (byMobile.isPresent()) {
                return byMobile;
            }
        }
        return userRepository.findByUsernameIgnoreCase(identifier);
    }

    private String validateDateOfBirth(String raw) {
        try {
            LocalDate dob = LocalDate.parse(raw.trim());
            if (!dob.isBefore(LocalDate.now()) || dob.isBefore(LocalDate.of(1900, 1, 1))) {
                throw new IllegalArgumentException("Date of birth must be a past date after 1900");
            }
            return dob.toString();
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Date of birth is not a valid date");
        }
    }

    private String normalizeMobile(String raw) {
        String cleaned = raw.replaceAll("[\\s()\\-]", "");
        if (!cleaned.matches(MOBILE_DIGITS)) {
            throw new IllegalArgumentException("Mobile number must be 7-15 digits, optionally starting with +");
        }
        return cleaned;
    }

    private AuthResponse toResponse(UserEntity user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId().toString());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .username(user.getUsername())
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
