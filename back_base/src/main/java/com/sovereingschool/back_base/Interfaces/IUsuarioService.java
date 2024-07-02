package com.sovereingschool.back_base.Interfaces;

import java.util.List;

import com.sovereingschool.back_base.DTOs.NewUsuario;
import com.sovereingschool.back_base.Models.Curso;
import com.sovereingschool.back_base.Models.Plan;
import com.sovereingschool.back_base.Models.Usuario;

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

    public List<Usuario> getProfes();
}
