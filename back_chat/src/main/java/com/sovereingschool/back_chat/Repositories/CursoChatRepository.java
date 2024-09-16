package com.sovereingschool.back_chat.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_chat.Models.CursoChat;

@Repository
public interface CursoChatRepository extends MongoRepository<CursoChat, String> {

}
