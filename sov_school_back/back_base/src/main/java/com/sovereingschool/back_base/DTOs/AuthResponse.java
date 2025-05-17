package com.sovereingschool.back_base.DTOs;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sovereingschool.back_common.Models.Usuario;

@JsonPropertyOrder({ "status", "message", "usuario", "accessToken", "refreshToken" })
public record AuthResponse(Boolean status, String message, Usuario usuario, String accessToken, String refreshToken) {
}
