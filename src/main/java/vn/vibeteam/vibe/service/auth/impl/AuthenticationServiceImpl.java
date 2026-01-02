package vn.vibeteam.vibe.service.auth.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.vibeteam.vibe.dto.request.authentication.LoginRequest;
import vn.vibeteam.vibe.dto.request.authentication.LogoutRequest;
import vn.vibeteam.vibe.dto.request.authentication.RegisterRequest;
import vn.vibeteam.vibe.dto.response.authentication.AuthenticationResponse;
import vn.vibeteam.vibe.exception.AppException;
import vn.vibeteam.vibe.exception.ErrorCode;
import vn.vibeteam.vibe.model.authorization.User;
import vn.vibeteam.vibe.repository.user.UserRepository;
import vn.vibeteam.vibe.service.auth.AuthenticationService;
import vn.vibeteam.vibe.service.auth.TokenBlacklistService;
import vn.vibeteam.vibe.util.JwtUtil;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    private final UserRepository userRepository;

    @Override
    public AuthenticationResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());

        User user = userRepository.findByUsername(loginRequest.getUsername())
                                  .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Invalid credentials for user: {}", loginRequest.getUsername());
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        List<String> roleNames = userRepository.findUserRolesByUserId(user.getId())
                                               .orElse(new ArrayList<>())
                                               .stream().map(userRole -> userRole.getRole().getName())
                                               .toList();
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roleNames);

        log.info("User {} logged in successfully", loginRequest.getUsername());
        return AuthenticationResponse.builder()
                                     .accessToken(jwtUtil.generateToken(user.getId().toString(), claims))
                                     .refreshToken(jwtUtil.generateRefreshToken(user.getId().toString(), claims))
                                     .build();
    }

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest registerRequest) {
        log.info("Register attempt for user: {}", registerRequest.getUsername());

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            log.warn("Username {} is already taken", registerRequest.getUsername());
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        User newUser = User.builder()
                           .username(registerRequest.getUsername())
                           .password(passwordEncoder.encode(registerRequest.getPassword()))
                           .build();
        User savedUser = userRepository.save(newUser);

        // Assign default role to the new user
        log.info("User {} registered successfully", savedUser.getUsername());
        return AuthenticationResponse.builder()
                                     .accessToken(jwtUtil.generateToken(savedUser.getId().toString(), Map.of()))
                                     .refreshToken(jwtUtil.generateRefreshToken(savedUser.getId().toString(), Map.of()))
                                     .build();
    }

    @Override
    public Boolean logout(LogoutRequest logoutRequest) {
        log.info("Logout attempt with access token: {}", logoutRequest.getAccessToken());

        long tokenExpiration = jwtUtil.getTokenExpiration(logoutRequest.getAccessToken());
        long refreshTokenExpiration = jwtUtil.getTokenExpiration(logoutRequest.getRefreshToken());

        if (!tokenBlacklistService.blacklistToken(logoutRequest.getRefreshToken(), tokenExpiration)) {
            log.error("Failed to blacklist access token");
            return false;
        }

        if (!tokenBlacklistService.blacklistToken(logoutRequest.getAccessToken(), refreshTokenExpiration)) {
            log.error("Failed to blacklist refresh token");
            return false;
        }

        log.info("Tokens blacklisted successfully for logout");
        return true;
    }
}
