package com.sovereingschool.back_streaming.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_streaming.Models.Curso;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

}
