package com.sovereingschool.back.Interfaces;

import java.util.List;

import com.sovereingschool.back.Models.Curso;
import com.sovereingschool.back.Models.Plan;
import com.sovereingschool.back.Models.Usuario;

public interface IUsuarioService {
    public String createUsuario(Usuario new_usuario);

    public String getNombreUsuario(Long id_usuario);

    public List<String> getFotosUsuario(Long id_usuario);

    public int getRollUsuario(Long id_usuario);

    public Plan getPlanUsuario(Long id_usuario);

    public List<Curso> getCursosUsuario(Long id_usuario);

    public String changeNombreUsuario(Usuario usuario);

    public String changeFotosUsuario(Usuario usuario);

    public String changePlanUsuario(Usuario usuario);

    public String changeCursosUsuario(Usuario usuario);

    public String deleteUsuario(Long id);
}
