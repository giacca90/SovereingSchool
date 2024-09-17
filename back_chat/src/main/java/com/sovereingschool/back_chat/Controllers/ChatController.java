package com.sovereingschool.back_chat.Controllers;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sovereingschool.back_chat.Models.MensajeChat;

@Controller
@RequestMapping("/chat")
public class ChatController {
    @MessageMapping("/chat/{CursoId}")
    @SendTo("/curso/{CursoId}")
    public MensajeChat chat(@DestinationVariable String CursoId, MensajeChat message) {
        return new MensajeChat(message.getIdMensaje(), message.getIdCurso(), message.getIdClase(),
                message.getIdUsuario(), message.getMensaje());
    }
}
