package com.sovereingschool.back.Models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
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
@Table(name = "plan")
public class Plan implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_plan;

    @Column(unique = true, nullable = false)
    private String nombre_plan;

    @Column(nullable = false)
    private BigDecimal precio_plan;

    @Column(nullable = false)
    @ManyToMany(mappedBy = "planes", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Curso> cursos;

    @Transient
    private List<Long> cursos_plan;
}