package com.myeshopping.authservice.service;

import com.myeshopping.authservice.entity.UserEntity;
import com.myeshopping.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Inactive user");
        }

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .disabled(!user.isActive())
                .build();
    }

    /**
     * Per-request lookup used by the JWT filter. Cached without the password hash, and as a separate
     * instance from the one the login flow authenticates against (and erases credentials on).
     * Evicted by {@link AdminUserService} when a user's role or active flag changes.
     */
    @Cacheable(cacheNames = "tokenUsers", key = "#email")
    public UserDetails loadTokenUser(String email) {
        return User.withUserDetails(loadUserByUsername(email)).password("").build();
    }
}
