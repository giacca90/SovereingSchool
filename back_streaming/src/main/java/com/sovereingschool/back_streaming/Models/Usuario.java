package com.sovereingschool.back_streaming.Models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
@Table(name = "usuario")
public class Usuario implements Serializable {
    @Id
    private Long id_usuario;

    private String nombre_usuario;

    private List<String> foto_usuario;

    private Integer roll_usuario;

    private Plan plan_usuario;

    private List<Curso> cursos_usuario;

    private Date fecha_registro_usuario;
}