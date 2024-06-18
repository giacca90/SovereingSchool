package com.sovereingschool.back.Models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
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
	@JsonIgnoreProperties({ "id_usuario", "foto_usuario", "roll_usuario", "plan_usuario", "cursos_usuario",
			"fecha_registro_usuario" })
	private List<Usuario> profesores_curso;

	private Date fecha_publicacion_curso;

	@OneToMany(mappedBy = "curso_clase", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JsonIgnoreProperties({ "id_clase", "tipo_clase", "direccion_clase", "posicion_clase", "curso_clase" })
	private List<Clase> clases_curso;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "cursos_plan", joinColumns = @JoinColumn(name = "id_curso"), inverseJoinColumns = @JoinColumn(name = "id_plan"))
	@JsonIgnoreProperties({ "id_plan", "precio_plan", "cursos_plan" })
	private List<Plan> planes_curso;

	private BigDecimal precio_curso;

}