package com.sovereingschool.back_streaming.Models;

import java.io.Serializable;
import java.math.BigDecimal;
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
@Table(name = "plan")
public class Plan implements Serializable {
    @Id
    private Long id_plan;

    private String nombre_plan;

    private BigDecimal precio_plan;

    private List<Curso> cursos_plan;

}