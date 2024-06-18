package com.sovereingschool.back.DTOs;

import java.util.Date;
import java.util.List;

import com.sovereingschool.back.Models.Curso;
import com.sovereingschool.back.Models.Plan;

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

	private Plan plan_usuario;

	private List<Curso> cursos_usuario;

	private Date fecha_registro_usuario;
}
