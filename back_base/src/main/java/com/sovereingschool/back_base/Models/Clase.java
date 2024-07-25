package com.sovereingschool.back_base.Models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_clase;

    @Column(nullable = false)
    private String nombre_clase;

    @Column(length = 1000)
    private String descriccion_clase;

    @Column(length = 10000)
    private String contenido_clase;

    @Column(nullable = false)
    private int tipo_clase;

    /*
     * @Column(nullable = false)
     * private String direccion_clase;
     */

    @Column()
    private String direccion_clase;

    @Column(nullable = false)
    private Integer posicion_clase;

    @ManyToOne
    @JoinColumn(name = "id_curso", nullable = false)
    // @JsonIgnore
    @JsonBackReference
    private Curso curso_clase;
}