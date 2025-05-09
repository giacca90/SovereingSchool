package com.sovereingschool.back_chat.Controllers;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sovereingschool.back_chat.DTOs.CursoChatDTO;
import com.sovereingschool.back_chat.Services.CursoChatService;
import com.sovereingschool.back_chat.Services.InitChatService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@PreAuthorize("hasAnyRole('USER', 'PROF', 'ADMIN')")
public class ChatController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private InitChatService initChatService;

    @Autowired
    private CursoChatService cursoChatService;

    /* Sección para el websocket */
    @MessageMapping("/init")
    @SendTo("/init_chat/result")
    public Object handleInitChat() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return "Token inválido: no autenticado";
            }
            Long idUsuario = (Long) authentication.getDetails(); // 👈 aquí recuperas el ID
            return initChatService.initChat(idUsuario);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return e.getMessage();
        } catch (Exception e) {
            System.err.println("Error en el websocket de init: " + e.getMessage());
            return "Error en obtener en init del chat: " + e.getMessage();
        }
    }

    @MessageMapping("/curso")
    public void getCursoChat(String message) {
        Long idCurso = Long.parseLong(message);
        try {
            CursoChatDTO cursoChat = cursoChatService.getCursoChat(idCurso);
            messagingTemplate.convertAndSend("/init_chat/" + idCurso, cursoChat);
        } catch (EntityNotFoundException e) {
            messagingTemplate.convertAndSend("/init_chat/" + idCurso, e.getMessage());
        } catch (Exception e) {
            System.err.println("Error en obtener el chat del curso: " + e.getMessage());
            messagingTemplate.convertAndSend("/init_chat/" + idCurso,
                    "Error en obtener el chat del curso: " + e.getMessage());
        }
    }

    @MessageMapping("/chat")
    public void guardaMensaje(String message) {
        try {
            this.cursoChatService.guardaMensaje(message);
        } catch (IllegalArgumentException | NoSuchElementException | DataAccessException e) {
            messagingTemplate.convertAndSend("/chat/", e.getMessage());
        } catch (Exception e) {
            System.err.println("Error en guardar mensaje: " + e.getMessage());
            messagingTemplate.convertAndSend("/chat/", "Error en guardar mensaje: " + e.getMessage());
        }
    }

    @MessageMapping("/leido")
    public void mensajeLeido(String message) {
        try {
            this.cursoChatService.mensajeLeido(message);
        } catch (EntityNotFoundException e) {
            messagingTemplate.convertAndSend("/leido", e.getMessage());
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/leido", "Error en marcar el mensaje como leido: " + e.getMessage());
        }
    }

    @MessageMapping("/refresh-token")
    public void refreshToken(@Header("simpSessionId") String sessionId, String newToken) {
        try {
            this.cursoChatService.refreshTokenInOpenWebsocket(sessionId, newToken);
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/refresh-token", "Error en refrescar el token: " + e.getMessage());
            // Devuelvo un error para cerrar el websocket
            throw new RuntimeException("Error en refrescar el token: " + e.getMessage());
        }
    }

    /* Sección para REST */
    @PostMapping("/crea_usuario_chat")
    public ResponseEntity<?> creaUsuarioChat(@RequestBody String message) {
        try {
            this.cursoChatService.creaUsuarioChat(message);
            return new ResponseEntity<String>("Usuario chat creado con exito!!!", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Error en crear el usuario del chat: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/crea_curso_chat")
    public ResponseEntity<?> creaCursoChat(@RequestBody String message) {
        try {
            this.cursoChatService.creaCursoChat(message);
            return new ResponseEntity<String>("Curso chat creado con exito!!!", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al crear en chat del curso: " + e.getCause(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/crea_clase_chat")
    public ResponseEntity<?> creaClaseChat(@RequestBody String message) {
        try {
            this.cursoChatService.creaClaseChat(message);
            return new ResponseEntity<String>("Clase chat creado con exito!!!", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Error en crear el chat de la clase: " + e.getCause(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete_clase_chat/{idCurso}/{idClase}")
    public ResponseEntity<?> borrarClaseChat(@PathVariable Long idCurso, @PathVariable Long idClase) {
        try {
            this.cursoChatService.borrarClaseChat(idCurso, idClase);
            return new ResponseEntity<String>("Clase chat borrado con exito!!!", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Error en borrar el chat del curso: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Función temporal para desarrollo
     * TODO: Eliminar en producción
     * 
     * @return
     */
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