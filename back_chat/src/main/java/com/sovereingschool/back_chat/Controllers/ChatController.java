package com.sovereingschool.back_chat.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.sovereingschool.back_chat.DTOs.InitChatDTO;
import com.sovereingschool.back_chat.Services.InitChatService;

@Controller
@CrossOrigin(origins = "http://localhost:4200, https://giacca90.github.io")
public class ChatController {
    @Autowired
    private InitChatService initChat;

    @MessageMapping("/init")
    @SendTo("/init_chat/result")
    public InitChatDTO handleInitChat(String message) {
        System.out.println("LLEGADA LA LLAMADA A INIT_CHAT: " + message);
        Long idUsuario = Long.parseLong(message);

        InitChatDTO initChat = this.initChat.initChat(idUsuario);
        System.out.println("SE DEVUELVE: " + initChat.toString());
        return initChat; // Objeto que representa el estado inicial
    }

}