package com.fiap.mecanica.os.presentation.controller;

import com.fiap.mecanica.os.infra.seeding.entity.UserSeedEntity;
import com.fiap.mecanica.os.infra.seeding.repository.UserSeedRepository;
import com.fiap.mecanica.os.infra.security.JwtService;
import com.fiap.mecanica.os.presentation.dto.LoginRequest;
import com.fiap.mecanica.os.presentation.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserSeedRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    UserSeedEntity user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    }

    String token = jwtService.generateToken(user.getEmail());
    return ResponseEntity.ok(new LoginResponse(token, "Bearer"));
  }
}
