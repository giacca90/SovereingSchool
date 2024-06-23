package com.sovereingschool.back_streaming.Models;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Entity;
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
@Entity
@Document(collection = "user_courses")
public class UsuarioCursos implements Serializable {
    @Id
    private Long id;
    private Long id_usuario;
    private List<CourseStatus> cursos;

    // Getters and setters
}

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
class CourseStatus {
    private Long id_curso;
    private List<ClassStatus> clases;

    // Getters and setters
}

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
class ClassStatus {
    private Long id_clase;
    private boolean completed;
    private int progress; // Representa el progreso en la clase, por ejemplo en segundos o porcentaje

    // Getters and setters
}
