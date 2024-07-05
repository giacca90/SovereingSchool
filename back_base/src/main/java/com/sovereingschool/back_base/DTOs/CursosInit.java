package com.sovereingschool.back_base.DTOs;

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
public class CursosInit {

    private Long id_curso;

    private String nombre_curso;

    private List<ProfesInit> profesores_curso;

    private String descriccion_corta;

    private String imagen_curso;

}
