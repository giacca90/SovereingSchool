package com.sovereingschool.back_streaming.Interfaces;

import com.sovereingschool.back_streaming.Models.Usuario;

public interface IUsuarioCursosService {

    public void syncUserCourses();

    public String addNuevoUsuario(Usuario usuario);

    public String addCursoUsuario(Usuario usuario);

    public String getClase(Long id_usuario, Long id_curso, Long id_clase);

}
