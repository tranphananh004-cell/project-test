package com.example.project.service;

import com.example.project.dto.request.LoginRequest;
import com.example.project.dto.request.RefreshTokenRequest;
import com.example.project.dto.response.LoginResponse;
import com.example.project.dto.response.RefreshTokenResponse;
import com.example.project.entity.RefreshToken;
import com.example.project.entity.User;
import com.example.project.exception.AppException;
import com.example.project.exception.ErrorCode;
import com.example.project.repository.RefreshTokenRepository;
import com.example.project.repository.UserRepository;
import com.example.project.security.CustomUserDetails;
import com.example.project.security.CustomUserDetailsService;
import com.example.project.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    // ─── Login ────────────────────────────────────────────────────────────────

    /**
     * Xác thực username/password, trả về access token + refresh token.
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Để Spring Security xác thực (throw BadCredentialsException nếu sai)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Tạo access token
        String accessToken = jwtService.generateAccessToken(userDetails);

        // Tạo refresh token và lưu DB
        String refreshTokenValue = createAndSaveRefreshToken(userDetails.getId());

        // Lấy danh sách permissions để trả cho Angular
        Set<String> permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        log.info("User '{}' logged in successfully", userDetails.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationMs / 1000)
                .userId(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .permissions(permissions)
                .build();
    }

    // ─── Refresh ──────────────────────────────────────────────────────────────

    /**
     * Validate refresh token → trả về access token mới.
     * Áp dụng Refresh Token Rotation: revoke token cũ, tạo token mới.
     */
    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

        if (!refreshToken.isValid()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        User user = refreshToken.getUser();

        // Load user với đầy đủ roles/permissions để tạo UserDetails
        CustomUserDetails userDetails = loadUserDetails(user.getUsername());

        // Tạo access token mới
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        // Refresh Token Rotation: revoke token cũ
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // Tạo refresh token mới (rotation)
        String newRefreshToken = createAndSaveRefreshToken(user.getId());

        log.info("Token refreshed for user '{}'", user.getUsername());

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationMs / 1000)
                .build();
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    /**
     * Thu hồi refresh token của user (logout thiết bị hiện tại).
     */
    @Transactional
    public void logout(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.info("User '{}' logged out", refreshToken.getUser().getUsername());
    }

    /**
     * Thu hồi tất cả refresh token của user (logout tất cả thiết bị).
     */
    @Transactional
    public void logoutAll(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        java.util.List<RefreshToken> activeTokens = refreshTokenRepository.findByUserAndRevokedFalse(user);
        for (RefreshToken token : activeTokens) {
            token.setRevoked(true);
        }
        refreshTokenRepository.saveAll(activeTokens);
        log.info("All tokens revoked for user id={}", userId);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private String createAndSaveRefreshToken(Long userId) {
        User user = userRepository.getReferenceById(userId);
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    private CustomUserDetails loadUserDetails(String username) {
        return (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);
    }
}
