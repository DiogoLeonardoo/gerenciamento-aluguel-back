package com.inhouse.project.service.dto;

public record AuthResponseDTO(
    String token,
    String email,
    String role
) {}
