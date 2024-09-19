package com.sovereingschool.back_chat.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sovereingschool.back_chat.DTOs.InitChatDTO;
import com.sovereingschool.back_chat.Models.UsuarioChat;
import com.sovereingschool.back_chat.Repositories.UsuarioChatRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class InitChatService {
    @Autowired
    private UsuarioChatRepository usuarioRepo;

    public InitChatDTO initChat(Long idUsuario) {
        UsuarioChat usuarioChat = this.usuarioRepo.findByIdUsuario(idUsuario);
        if (usuarioChat == null) {
            usuarioChat = new UsuarioChat(null, 0L, null, null);
        }
        return new InitChatDTO(usuarioChat.getIdUsuario(), usuarioChat.getCursos(), usuarioChat.getMensajes());
    }

}
