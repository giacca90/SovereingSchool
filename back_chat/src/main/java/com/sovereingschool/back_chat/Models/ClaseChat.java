package com.sovereingschool.back_chat.Models;

import java.util.List;

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
public class ClaseChat {
    @Id
    private Long idClase;

    private Long idCurso;

    private List<MensajeChat> mensajes;

}