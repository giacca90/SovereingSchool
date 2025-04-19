package com.sovereingschool.back_chat.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_chat.Models.MensajeChat;

@Repository
public interface MensajeChatRepository extends MongoRepository<MensajeChat, String> {

}
