package com.sovereingschool.back_chat.Models;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "messages_chat")
public class MensajeChat {
    @Id
    private String id;

    private Long idCurso;

    private Long idClase;

    private Long idUsuario;

    private String respuesta;

    private String mensaje;

    private Date fecha;

}