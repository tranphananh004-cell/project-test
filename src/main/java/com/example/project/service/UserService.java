package com.example.project.service;

import com.example.project.dto.request.AssignRolesRequest;
import com.example.project.dto.request.UserCreateRequest;
import com.example.project.dto.request.UserUpdateRequest;
import com.example.project.dto.response.PageResponse;
import com.example.project.dto.response.UserResponse;
import com.example.project.entity.Role;
import com.example.project.entity.User;
import com.example.project.exception.AppException;
import com.example.project.exception.ErrorCode;
import com.example.project.repository.RoleRepository;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<User> userPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            userPage = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                    kw, kw, kw, pageable
            );
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return PageResponse.of(userPage.map(UserResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .fullName(request.fullName())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .build();

        if (request.roleIds() != null && !request.roleIds().isEmpty()) {
            Set<Role> roles = roleRepository.findByIdIn(request.roleIds());
            user.setRoles(roles);
        } else {
            // Gán role mặc định USER nếu không chọn role
            roleRepository.findByName("USER").ifPresent(role -> user.getRoles().add(role));
        }

        User saved = userRepository.save(user);
        log.info("Created user: {} (id: {})", saved.getUsername(), saved.getId());
        return UserResponse.fromEntity(saved);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.email() != null && !user.getEmail().equalsIgnoreCase(request.email())
                && userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (request.email() != null) user.setEmail(request.email());
        if (request.fullName() != null) user.setFullName(request.fullName());

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }

        if (request.roleIds() != null) {
            Set<Role> roles = roleRepository.findByIdIn(request.roleIds());
            user.setRoles(roles);
        }

        User updated = userRepository.save(user);
        log.info("Updated user: {} (id: {})", updated.getUsername(), updated.getId());
        return UserResponse.fromEntity(updated);
    }

    @Transactional
    public UserResponse assignRolesToUser(Long id, AssignRolesRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Set<Role> roles = new HashSet<>();
        if (request.roleIds() != null && !request.roleIds().isEmpty()) {
            roles = roleRepository.findByIdIn(request.roleIds());
        }

        user.setRoles(roles);
        User updated = userRepository.save(user);
        log.info("Assigned {} roles to user: {}", roles.size(), updated.getUsername());
        return UserResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new AppException(ErrorCode.CANNOT_DELETE_ADMIN_USER);
        }

        user.getRoles().clear();
        userRepository.delete(user);
        log.info("Deleted user: {} (id: {})", user.getUsername(), id);
    }
}
