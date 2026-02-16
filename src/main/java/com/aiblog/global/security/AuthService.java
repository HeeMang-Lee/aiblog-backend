package com.aiblog.global.security;

import com.aiblog.global.exception.BusinessException;
import com.aiblog.global.exception.ErrorCode;
import com.aiblog.global.security.dto.LoginRequest;
import com.aiblog.global.security.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final String adminUsername;
  private final String adminPassword;
  private final JwtProvider jwtProvider;

  public AuthService(
      @Value("${app.admin.username}") String adminUsername,
      @Value("${app.admin.password}") String adminPassword,
      JwtProvider jwtProvider) {
    this.adminUsername = adminUsername;
    this.adminPassword = adminPassword;
    this.jwtProvider = jwtProvider;
  }

  public TokenResponse login(LoginRequest request) {
    if (!adminUsername.equals(request.username())
        || !adminPassword.equals(request.password())) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }
    String token = jwtProvider.generateToken(request.username());
    return new TokenResponse(token);
  }
}
