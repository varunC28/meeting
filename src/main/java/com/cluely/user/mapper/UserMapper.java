package com.cluely.user.mapper;

import com.cluely.user.dto.UserCreateRequestDto;
import com.cluely.user.dto.UserResponseDto;
import com.cluely.user.entity.UserEntity;

public class UserMapper {

    private UserMapper() {
        // utility class
    }

    // INPUT: API → DOMAIN
    public static UserEntity toEntity(UserCreateRequestDto dto) {
        UserEntity entity = new UserEntity();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setContactNumber(dto.getContactNumber());
        // entity.setPasswordHash(dto.getPassword());

        // Backend-owned defaults
        entity.setStatus("ACTIVE");

        return entity;
    }

    // OUTPUT: DOMAIN → API
    public static UserResponseDto toResponse(UserEntity entity) {
        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(entity.getUserId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setContactNumber(entity.getContactNumber());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
