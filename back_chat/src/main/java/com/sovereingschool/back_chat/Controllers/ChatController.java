package com.sovereingschool.back_chat.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.sovereingschool.back_chat.DTOs.CursoChatDTO;
import com.sovereingschool.back_chat.DTOs.InitChatDTO;
import com.sovereingschool.back_chat.Services.CursoChatService;
import com.sovereingschool.back_chat.Services.InitChatService;

@Controller
@CrossOrigin(origins = "http://localhost:4200, https://giacca90.github.io")
public class ChatController {
    @Autowired
    private InitChatService initChatService;

    @Autowired
    private CursoChatService cursoChatService;

    @MessageMapping("/init")
    @SendTo("/init_chat/result")
    public InitChatDTO handleInitChat(String message) {
        System.out.println("LLEGADA LA LLAMADA A INIT_CHAT: " + message);
        Long idUsuario = Long.parseLong(message);

        InitChatDTO initChat = this.initChatService.initChat(idUsuario);
        System.out.println("SE DEVUELVE: " + initChat.toString());
        return initChat; // Objeto que representa el estado inicial
    }

    @MessageMapping("/curso")
    @SendTo("/init_chat/${idCurso}")
    public CursoChatDTO getCursoChat(String message) {
        System.out.println("LLEGADA LA LLAMADA A INIT_CURSO: " + message);
        Long idCurso = Long.parseLong(message);

        CursoChatDTO cursoChat = this.cursoChatService.getChat(idCurso);
        if (cursoChat != null) {
            System.out.println("SE DEVUELVE: " + cursoChat.toString());
        }
        return cursoChat; // Objeto que representa el estado inicial
    }
}