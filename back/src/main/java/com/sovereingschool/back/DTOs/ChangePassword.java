package com.sovereingschool.back.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChangePassword {
	private Long id_usuario;
	private String old_password;
	private String new_password;
}
