package com.sovereingschool.back_base.Interfaces;

import java.util.List;

import com.sovereingschool.back_base.DTOs.AuthResponse;
import com.sovereingschool.back_base.DTOs.NewUsuario;
import com.sovereingschool.back_common.Models.Curso;
import com.sovereingschool.back_common.Models.Plan;
import com.sovereingschool.back_common.Models.RoleEnum;
import com.sovereingschool.back_common.Models.Usuario;

public interface IUsuarioService {
    public AuthResponse createUsuario(NewUsuario new_usuario);

    public Usuario getUsuario(Long id_usuario);

    public String getNombreUsuario(Long id_usuario);

    public List<String> getFotosUsuario(Long id_usuario);

    public RoleEnum getRollUsuario(Long id_usuario);

    public Plan getPlanUsuario(Long id_usuario);

    public List<Curso> getCursosUsuario(Long id_usuario);

    public Usuario updateUsuario(Usuario usuario);

    public Integer changePlanUsuario(Usuario usuario);

    public Integer changeCursosUsuario(Usuario usuario);

    public String deleteUsuario(Long id);

    public List<Usuario> getProfes();
}
