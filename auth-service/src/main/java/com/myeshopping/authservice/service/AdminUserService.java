package com.myeshopping.authservice.service;

import com.myeshopping.authservice.dto.AdminUserResponse;
import com.myeshopping.authservice.entity.Role;
import com.myeshopping.authservice.entity.UserEntity;
import com.myeshopping.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Role rules: SUPER_ADMIN can manage everyone except themselves; ADMIN can only activate/deactivate
 * CUSTOMER accounts; only SUPER_ADMIN can promote/demote admins; nobody can change a SUPER_ADMIN.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(UserEntity::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdminUserResponse setActive(UUID id, boolean active, String actorEmail) {
        UserEntity actor = actor(actorEmail);
        UserEntity target = find(id);
        rejectSelf(actor, target, "status");
        rejectSuperAdminTarget(target);
        if (actor.getRole() != Role.SUPER_ADMIN && target.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException("Only the super admin can activate or deactivate another admin");
        }
        target.setActive(active);
        return toResponse(userRepository.save(target));
    }

    @Transactional
    public AdminUserResponse setRole(UUID id, String role, String actorEmail) {
        UserEntity actor = actor(actorEmail);
        UserEntity target = find(id);
        if (actor.getRole() != Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("Only the super admin can change roles");
        }
        rejectSelf(actor, target, "role");
        rejectSuperAdminTarget(target);
        Role newRole;
        try {
            newRole = Role.valueOf(role == null ? "" : role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown role: " + role);
        }
        if (newRole == Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("The super admin role cannot be assigned here");
        }
        target.setRole(newRole);
        return toResponse(userRepository.save(target));
    }

    private void rejectSelf(UserEntity actor, UserEntity target, String what) {
        if (actor.getId().equals(target.getId())) {
            throw new IllegalArgumentException("You cannot change your own " + what);
        }
    }

    private void rejectSuperAdminTarget(UserEntity target) {
        if (target.getRole() == Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("The super admin cannot be changed");
        }
    }

    private UserEntity actor(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Acting user not found"));
    }

    private UserEntity find(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    private AdminUserResponse toResponse(UserEntity user) {
        return AdminUserResponse.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .username(user.getUsername())
                .mobileNumber(user.getMobileNumber())
                .role(user.getRole().name())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
