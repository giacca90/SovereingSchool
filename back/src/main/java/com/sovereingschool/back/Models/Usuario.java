package com.sovereingschool.back.Models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.Type;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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
@Table(name = "usuario")
public class Usuario implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_usuario;

    @Column(nullable = false)
    private String nombre_usuario;

    @Column(unique = true, columnDefinition = "text[]")
    @Type(com.vladmihalcea.hibernate.type.array.ListArrayType.class)
    private List<String> foto_usuario;

    @Column(nullable = false)
    private Integer roll_usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_usuario", referencedColumnName = "id_plan")
    @JsonIgnore
    private Plan plan;

    @Transient
    private Long plan_usuario;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "usuario_curso", joinColumns = @JoinColumn(name = "id_usuario"), inverseJoinColumns = @JoinColumn(name = "id_curso"))
    @JsonIgnore
    private List<Curso> cursos;

    @Transient
    private List<Long> cursos_usuario;

    @Temporal(TemporalType.DATE)
    private Date fecha_registro_usuario;
}