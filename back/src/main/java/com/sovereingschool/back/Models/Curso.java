package com.sovereingschool.back.Models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
import jakarta.persistence.Transient;
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "curso_profesor", joinColumns = @JoinColumn(name = "id_curso"), inverseJoinColumns = @JoinColumn(name = "id_usuario"))
    @JsonIgnore
    private List<Usuario> profesores;

    @Transient
    private List<String> profesores_curso;

    private Date fecha_publicacion_curso;

    @OneToMany(mappedBy = "curso", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Clase> clases;

    @Transient
    private List<String> clases_curso;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "cursos_plan", joinColumns = @JoinColumn(name = "id_curso"), inverseJoinColumns = @JoinColumn(name = "id_plan"))
    @JsonIgnore
    private List<Plan> planes;

    @Transient
    private List<String> planes_curso;

    private BigDecimal precio_curso;

    public List<String> getProfesores_curso() {
        if (profesores != null) {
            return profesores.stream().map(Usuario::getNombre_usuario).collect(Collectors.toList());
        }
        return profesores_curso;
    }

    public List<String> getClases_curso() {
        if (clases != null) {
            return clases.stream().map(Clase::getNombre_clase).collect(Collectors.toList());
        }
        return clases_curso;
    }

    public List<String> getPlanes_curso() {
        if (planes != null) {
            return planes.stream().map(Plan::getNombre_plan).collect(Collectors.toList());
        }
        return clases_curso;
    }

}