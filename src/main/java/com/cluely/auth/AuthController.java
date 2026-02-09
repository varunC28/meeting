package com.cluely.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.cluely.auth.dto.AuthResponseDto;
import com.cluely.security.JwtService;
import com.cluely.user.dto.LoginRequestDto;
import com.cluely.user.repository.UserRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository repository;
    private final BCryptPasswordEncoder encoder;

    public AuthController(
            UserRepository repository,
            BCryptPasswordEncoder encoder,
            JwtService jwtService) {

        this.repository = repository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody LoginRequestDto dto) {

        var user = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        if (!encoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(
                user.getUserId().toString(),
                user.getEmail());

        return new AuthResponseDto(token);
    }
}
