package com.sovereingschool.back_chat.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_chat.Models.Curso;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

}
