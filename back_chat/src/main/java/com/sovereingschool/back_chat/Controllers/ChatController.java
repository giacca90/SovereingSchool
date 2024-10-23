package com.sovereingschool.back_chat.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.sovereingschool.back_chat.DTOs.CursoChatDTO;
import com.sovereingschool.back_chat.DTOs.InitChatDTO;
import com.sovereingschool.back_chat.Services.CursoChatService;
import com.sovereingschool.back_chat.Services.InitChatService;

@Controller
@CrossOrigin(origins = "http://localhost:4200, https://giacca90.github.io")
public class ChatController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private InitChatService initChatService;

    @Autowired
    private CursoChatService cursoChatService;

    @MessageMapping("/init")
    @SendTo("/init_chat/result")
    public InitChatDTO handleInitChat(String message) {
        // System.out.println("LLEGADA LA LLAMADA A INIT_CHAT: " + message);
        Long idUsuario = Long.parseLong(message);

        InitChatDTO initChat = this.initChatService.initChat(idUsuario);
        // System.out.println("SE DEVUELVE: " + initChat.toString());
        return initChat; // Objeto que representa el estado inicial
    }

    @MessageMapping("/curso")
    public void getCursoChat(String message) {
        // System.out.println("LLEGADA LA LLAMADA A INIT_CURSO: " + message);
        Long idCurso = Long.parseLong(message);

        CursoChatDTO cursoChat = cursoChatService.getChat(idCurso);
        if (cursoChat != null) {
            // System.out.println("SE DEVUELVE: " + cursoChat.toString());
            // Enviar el mensaje a un destino dinámico usando SimpMessagingTemplate
            messagingTemplate.convertAndSend("/init_chat/" + idCurso, cursoChat);
        }
    }

    @MessageMapping("/chat")
    public void guardaMensaje(String message) {
        this.cursoChatService.guardaMensaje(message);
    }

    @PostMapping("/crea_usuario_chat")
    public ResponseEntity<?> creaUsuarioChat(@RequestBody String message) {
        try {
            this.cursoChatService.creaUsuarioChat(message);
            return new ResponseEntity<String>("Usuario chat creado con exito!!!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error en crear en usuario del chat: " + e.getCause(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/crea_curso_chat")
    public ResponseEntity<?> creaCursoChat(@RequestBody String message) {
        try {
            this.cursoChatService.creaCursoChat(message);
            return new ResponseEntity<String>("Curso chat creado con exito!!!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error en crear en curso del chat: " + e.getCause(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/init")
    public ResponseEntity<?> init() {
        try {
            this.cursoChatService.init();
            return new ResponseEntity<String>("Iniciado mongo con exito!!!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}