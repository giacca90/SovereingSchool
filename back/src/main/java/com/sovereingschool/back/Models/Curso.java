package com.sovereingschool.back.Models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_curso;

    @Column(unique = true, nullable = false)
    private String nombre_curso;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "curso_profesor", joinColumns = @JoinColumn(name = "id_curso"), inverseJoinColumns = @JoinColumn(name = "id_usuario"))
    private List<Usuario> profesores_curso;

    private Date fecha_publicacion_curso;

    @OneToMany(mappedBy = "id_clase", fetch = FetchType.LAZY)
    private List<Clase> clases_curso;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "cursos_plan", joinColumns = @JoinColumn(name = "id_curso"), inverseJoinColumns = @JoinColumn(name = "id_plan"))
    private List<Plan> planes_curso;

    private BigDecimal precio_curso;
}