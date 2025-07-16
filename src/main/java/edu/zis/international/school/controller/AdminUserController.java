package edu.zis.international.school.controller;

import edu.zis.international.school.repository.RoleRepository;
import edu.zis.international.school.repository.UserRepository;
import edu.zis.international.school.entity.Role;
import edu.zis.international.school.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignRoleToUser(
            @PathVariable Long userId,
            @RequestParam String roleName) {
        log.info("AdminUserController | Request received to update role for user: {} to {}", userId, roleName);
        // 1. Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Fetch role
        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        // 3. Assign new role
        user.setRole(role);
        userRepository.save(user);

        return ResponseEntity.ok("Role updated to: " + role.getName() + " for user " + userId);
    }
}

