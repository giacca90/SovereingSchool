package com.sovereingschool.back.Interfaces;

public interface ILoginService {
    public String createNuevoLogin(Long id_usuario, String correo_usuario, String password_usuario);

    public String getCorreoLogin(Long id_usuario);

    public String getPasswordLogin(Long id_usuario);

    public String changeCorreoLogin(Long id_usuario, String new_Correo_usuario);

    public String changePasswordLogin(Long id_usuario, String old_password, String new_password);

    public String deleteLogin(Long id_usuario);
}
