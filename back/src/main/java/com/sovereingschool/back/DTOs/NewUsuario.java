package com.sovereingschool.back.DTOs;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class NewUsuario {
	private String nombre_usuario;

	private String correo_electronico;

	private String password;

	private List<String> foto_usuario;

	private Integer roll_usuario;

	private Long plan_usuario;

	private List<Long> cursos_usuario;

	private Date fecha_registro_usuario;
}
