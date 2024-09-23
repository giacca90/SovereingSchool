package com.sovereingschool.back_chat.DTOs;

import java.io.Serializable;

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
public class MensajeChatDTO implements Serializable {
    private Long id_mensaje;
    private Long id_curso;
    private Long id_clase;
    private Long id_usuario;

    private String nombre_curso;
    private String nombre_clase;
    private String nombre_usuario;

    private String foto_curso;
    private String foto_usuario;

    private String mensaje;

}