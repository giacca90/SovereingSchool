package com.sovereingschool.back_streaming.Models;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "clase")
public class Clase implements Serializable {
    @Id
    private Long id_clase;

    private String nombre_clase;

    private int tipo_clase;

    private String direccion_clase;

    private Integer posicion_clase;

    private Curso curso_clase;
}