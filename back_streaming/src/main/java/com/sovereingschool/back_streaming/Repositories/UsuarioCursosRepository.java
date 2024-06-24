package com.sovereingschool.back_streaming.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_streaming.Models.UsuarioCursos;

@Repository
public interface UsuarioCursosRepository extends MongoRepository<UsuarioCursos, String> {
}
