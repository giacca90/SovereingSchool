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
public class UsuarioChat implements Serializable {
    @Id
    private Long idUsuario;

    private List<CursoChat> cursos;

    private List<MensajeChat> mensajes;

}