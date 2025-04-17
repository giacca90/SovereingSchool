package com.sovereingschool.back_base.DTOs;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sovereingschool.back_base.Models.Usuario;

@JsonPropertyOrder({ "status", "message", "userId", "accessToken" })
public record AuthResponse(Boolean status, String message, Usuario usuario, String accessToken) {
}
