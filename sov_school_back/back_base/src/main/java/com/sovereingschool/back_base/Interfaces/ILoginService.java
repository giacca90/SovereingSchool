package com.sovereingschool.back_base.Interfaces;

import com.sovereingschool.back_base.DTOs.AuthResponse;
import com.sovereingschool.back_base.DTOs.ChangePassword;
import com.sovereingschool.back_common.Models.Login;

public interface ILoginService {

    public Long compruebaCorreo(String correo);

    public String createNuevoLogin(Login login);

    public String getCorreoLogin(Long id_usuario);

    public String getPasswordLogin(Long id_usuario);

    public String changeCorreoLogin(Login login);

    public Integer changePasswordLogin(ChangePassword changepassword);

    public String deleteLogin(Long id_usuario);

    public AuthResponse loginUser(Long id, String password);
}
