package com.sovereingschool.back_chat.Repositories;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_chat.Models.CursoChat;

@Repository
public interface CursoChatRepository extends MongoRepository<CursoChat, String> {
    @Aggregation(pipeline = {
            "{ '$match': { 'id_curso' : ?0 } }",
            "{ '$project': { 'mensajes': { '$slice': ['$mensajes', 20] }, 'idCurso': 1, 'clases': 1 } }"
    })
    CursoChat findByIdCurso(Long id_curso);
}
