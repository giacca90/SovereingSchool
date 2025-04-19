package com.sovereingschool.back_chat.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_chat.Models.Curso;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

    @Query("SELECT c.nombre_curso FROM Curso c WHERE c.id_curso = :id")
    String findNombreCursoById(@Param("id") Long id);

    @Query("SELECT c.imagen_curso FROM Curso c WHERE c.id_curso = :id")
    String findImagenCursoById(@Param("id") Long id);

}
