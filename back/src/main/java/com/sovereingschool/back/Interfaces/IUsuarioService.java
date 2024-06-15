package com.sovereingschool.back.Interfaces;

import java.util.List;

import com.sovereingschool.back.Models.Curso;
import com.sovereingschool.back.Models.Plan;

public interface IUsuarioService {
    public String createUsuario(String nombre_usuario, List<String> fotos_usuario, Plan plan_usuario,
            List<Curso> cursos_usuario);

    public String getNombreUsuario(Long id_usuario);

    public List<String> getFotosUsuario(Long id_usuario);

    public int getRollUsuario(Long id_usuario);

    public Plan getPlanUsuario(Long id_usuario);

    public List<Curso> getCursosUsuario(Long id_usuario);

    public String changeNombreUsuario(Long id_usuario, String new_nombre_usuario);

    public String changeFotosUsuario(Long id_usuario, List<String> fotos_usuario);

    public String changePlanUsuario(Long id_usuario, Plan plan);

    public String changeCursosUsuario(Long id_usuario, List<Curso> cursos_usuario);

    public String deleteUsuario(Long id_usuario);
}
