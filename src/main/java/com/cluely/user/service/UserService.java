package com.cluely.user.service;

import com.cluely.user.repository.UserRepository;

import org.springframework.stereotype.Service;

import com.cluely.user.dto.UserCreateRequestDto;
import com.cluely.user.dto.UserResponseDto;
import com.cluely.user.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository repository, BCryptPasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public UserResponseDto createUser(UserCreateRequestDto dto) {

        if (repository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        var entity = UserMapper.toEntity(dto);
        entity.setPasswordHash(encoder.encode(dto.getPassword()));

        var saved = repository.save(entity);
        return UserMapper.toResponse(saved);
    }
}
