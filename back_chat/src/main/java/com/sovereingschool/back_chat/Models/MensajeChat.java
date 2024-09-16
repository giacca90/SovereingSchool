package com.sovereingschool.back_chat.Models;

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
public class MensajeChat {
    @Id
    private Long idMensaje;

    private Long idCurso;

    private Long idClase;

    private Long idUsuario;

    private String mensaje;

}