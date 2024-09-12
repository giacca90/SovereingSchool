package com.sovereingschool.back_chat.Models;

import java.io.Serializable;

import jakarta.persistence.Entity;
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
@Entity
public class MensajeChat implements Serializable {
    @Id
    private Long idMensaje;

    private Long idCurso;

    private Long idClase;

    private Long idUsuario;

    private String mensaje;

}