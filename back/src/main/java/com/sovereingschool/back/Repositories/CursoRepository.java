package com.sovereingschool.back.Repositories;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back.Models.Clase;
import com.sovereingschool.back.Models.Curso;
import com.sovereingschool.back.Models.Plan;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {
    @Query("SELECT c.nombre_curso FROM Curso c WHERE c.id_curso = :id")
    String findNombreCursoById(@Param("id") Long id);

    @Query("SELECT u.nombre_usuario FROM Usuario u WHERE u IN (SELECT c.profesores_curso FROM Curso c WHERE c.id_curso = :id)")
    List<String> findNombresProfesoresCursoById(@Param("id") Long id);

    @Query("SELECT c.fecha_publicacion_curso FROM Curso c WHERE c.id_curso = :id")
    Date findFechaCreacionCursoById(@Param("id") Long id);

    @Query("SELECT cl FROM Clase cl WHERE cl.curso_clase.id_curso = :id")
    List<Clase> findClasesCursoById(@Param("id") Long id);

    @Query("SELECT p FROM Plan p WHERE p IN (SELECT c.planes_curso FROM Curso c WHERE c.id_curso = :id)")
    List<Plan> findPlanesCursoById(@Param("id") Long id);

    @Query("SELECT c.precio_curso FROM Curso c WHERE c.id_curso = :id")
    BigDecimal findPrecioCursoById(@Param("id") Long id);

}
