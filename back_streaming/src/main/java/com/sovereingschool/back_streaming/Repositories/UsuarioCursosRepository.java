package com.sovereingschool.back_streaming.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_streaming.Models.UsuarioCursos;

@Repository
public interface UsuarioCursosRepository extends MongoRepository<UsuarioCursos, String> {

    @Query(value = "{ 'id_usuario' : ?0 }")
    UsuarioCursos findByIdUsuario(Long id_usuario);
}
