package com.sovereingschool.back_chat.Models;

import java.io.Serializable;
import java.util.List;

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
public class CursoChat implements Serializable {
    @Id
    private Long idCurso;

    private List<ClaseChat> clases;

    private List<MensajeChat> mensajes;

}