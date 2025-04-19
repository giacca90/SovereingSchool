package com.sovereingschool.back_base.DTOs;

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
public class Estadistica {
    int profesores;

    Long alumnos;

    Long cursos;

    Long clases;
}
