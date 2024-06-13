package com.sovereingschool.back.Models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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

    @Column(unique = true)
    private List<String> foto_usuario;

    private int roll_usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_usuario", referencedColumnName = "id_plan")
    private Plan plan_usuario;

    @OneToMany(mappedBy = "id_curso", fetch = FetchType.LAZY)
    private List<Curso> cursos_usuario;

    @Temporal(TemporalType.DATE)
    private Date fecha_registro_usuario;
}