package com.sovereingschool.back_streaming.Interfaces;

import com.sovereingschool.back_common.Models.Clase;
import com.sovereingschool.back_common.Models.Usuario;

public interface IUsuarioCursosService {

    public void syncUserCourses();

    public String addNuevoUsuario(Usuario usuario);

    public String addCursoUsuario(Usuario usuario);

    // public Boolean editClase(Long idCurso, Clase clase);

    public boolean addClase(Long idCurso, Clase clase);

    public String getClase(Long id_usuario, Long id_curso, Long id_clase);

}
