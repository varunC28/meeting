package com.cluely.user.controller;

import com.cluely.user.dto.UserCreateRequestDto;
import com.cluely.user.dto.UserResponseDto;
import com.cluely.user.service.UserService;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public UserResponseDto create(@Valid @RequestBody UserCreateRequestDto dto) {
        return service.createUser(dto);
    }
}
