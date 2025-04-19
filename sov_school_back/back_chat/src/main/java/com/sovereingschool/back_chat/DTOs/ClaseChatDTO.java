package com.sovereingschool.back_chat.DTOs;

import java.util.List;

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
public class ClaseChatDTO {

    private Long id_clase;

    private Long id_curso;

    private String nombre_clase;

    private List<MensajeChatDTO> mensajes;
}
