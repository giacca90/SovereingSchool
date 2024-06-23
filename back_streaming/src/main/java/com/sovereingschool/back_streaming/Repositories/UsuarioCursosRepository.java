package com.sovereingschool.back_streaming.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_streaming.Models.UsuarioCursos;

@Repository
public interface UsuarioCursosRepository extends JpaRepository<UsuarioCursos, Long> {
    UsuarioCursos findByUserId(Long id_usuario);
}
