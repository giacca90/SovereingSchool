package com.sovereingschool.back_streaming.Models;

import java.io.Serializable;
import java.math.BigDecimal;
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
@Table(name = "curso")
public class Curso implements Serializable {
    @Id
    private Long id_curso;

    private String nombre_curso;

    private List<Usuario> profesores_curso;

    private Date fecha_publicacion_curso;

    private List<Clase> clases_curso;

    private List<Plan> planes_curso;

    private BigDecimal precio_curso;

}