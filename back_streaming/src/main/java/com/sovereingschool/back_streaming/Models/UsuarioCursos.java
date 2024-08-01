package com.sovereingschool.back_streaming.Models;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
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
@Document(collection = "user_courses")
public class UsuarioCursos implements Serializable {
    @Id
    private String id;
    private Long id_usuario;
    private List<StatusCurso> cursos;
}
