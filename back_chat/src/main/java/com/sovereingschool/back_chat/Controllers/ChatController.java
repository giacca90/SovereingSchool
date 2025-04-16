package com.sovereingschool.back_chat.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sovereingschool.back_chat.DTOs.CursoChatDTO;
import com.sovereingschool.back_chat.DTOs.InitChatDTO;
import com.sovereingschool.back_chat.Services.CursoChatService;
import com.sovereingschool.back_chat.Services.InitChatService;

@RestController
@PreAuthorize("hasAnyRole('USER', 'PROF', 'ADMIN')")
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
        // ("LLEGADA LA LLAMADA A INIT_CHAT: " + message);
        Long idUsuario = Long.parseLong(message);

        InitChatDTO initChat = this.initChatService.initChat(idUsuario);
        // ("SE DEVUELVE: " + initChat.toString());
        return initChat; // Objeto que representa el estado inicial
    }

    @MessageMapping("/curso")
    public void getCursoChat(String message) {
        // ("LLEGADA LA LLAMADA A INIT_CURSO: " + message);
        Long idCurso = Long.parseLong(message);

        CursoChatDTO cursoChat = cursoChatService.getChat(idCurso);
        if (cursoChat != null) {
            // Enviar el mensaje a un destino dinámico usando SimpMessagingTemplate
            messagingTemplate.convertAndSend("/init_chat/" + idCurso, cursoChat);
        }
    }

    @MessageMapping("/chat")
    public void guardaMensaje(String message) {
        this.cursoChatService.guardaMensaje(message);
    }

    @MessageMapping("/leido")
    public void mensajeLeido(String message) {
        this.cursoChatService.mensajeLeido(message);
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

    @PostMapping("/crea_clase_chat")
    public ResponseEntity<?> creaClaseChat(@RequestBody String message) {
        try {
            this.cursoChatService.creaClaseChat(message);
            return new ResponseEntity<String>("Clase chat creado con exito!!!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error en crear en curso del chat: " + e.getCause(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/delete_clase_chat/{idCurso}/{idClase}")
    public ResponseEntity<?> borrarClaseChat(@PathVariable Long idCurso, @PathVariable Long idClase) {
        try {
            this.cursoChatService.borrarClaseChat(idCurso, idClase);
            return new ResponseEntity<String>("Clase chat borrado con exito!!!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error en borrar la clase del chat: " + e.getCause(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete_curso_chat/{idCurso}")
    public ResponseEntity<?> borrarCursoChat(@PathVariable Long idCurso) {
        try {
            this.cursoChatService.borrarCursoChat(idCurso);
            return new ResponseEntity<String>("Curso chat borrado con exito!!!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error en borrar el curso del chat: " + e.getCause(),
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