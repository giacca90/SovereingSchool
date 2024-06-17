package com.sovereingschool.back.Interfaces;

import java.util.List;

import com.sovereingschool.back.DTOs.NewUsuario;
import com.sovereingschool.back.Models.Curso;
import com.sovereingschool.back.Models.Plan;
import com.sovereingschool.back.Models.Usuario;

public interface IUsuarioService {
    public String createUsuario(NewUsuario new_usuario);

    public Usuario getUsuario(Long id_usuario);

    public String getNombreUsuario(Long id_usuario);

    public List<String> getFotosUsuario(Long id_usuario);

    public Integer getRollUsuario(Long id_usuario);

    public Plan getPlanUsuario(Long id_usuario);

    public List<Curso> getCursosUsuario(Long id_usuario);

    public Integer changeNombreUsuario(Usuario usuario);

    public Integer changeFotosUsuario(Usuario usuario);

    public Integer changePlanUsuario(Usuario usuario);

    public Integer changeCursosUsuario(Usuario usuario);

    public String deleteUsuario(Long id);
}
