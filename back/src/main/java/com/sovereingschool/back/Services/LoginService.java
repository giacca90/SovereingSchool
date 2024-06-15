package com.sovereingschool.back.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sovereingschool.back.Interfaces.ILoginService;
import com.sovereingschool.back.Models.Login;
import com.sovereingschool.back.Models.Usuario;
import com.sovereingschool.back.Repositories.LoginRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class LoginService implements ILoginService {

    @Autowired
    private LoginRepository repo;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public String createNuevoLogin(Long id_usuario, String correo_usuario, String password_usuario) {
        Usuario usuario = new Usuario();
        usuario.setId_usuario(id_usuario);
        this.repo.save(new Login(id_usuario, usuario, correo_usuario, password_usuario));
        return "Nuevo Usuario creado con Exito!!!";

    }

    @Override
    public String getCorreoLogin(Long id_usuario) {
        return this.repo.findCorreoLoginForId(id_usuario);
    }

    @Override
    public String getPasswordLogin(Long id_usuario) {
        return this.repo.findPasswordLoginForId(id_usuario);
    }

    @Override
    public String changeCorreoLogin(Long id_usuario, String new_Correo_usuario) {
        this.repo.changeCorreoLoginForId(id_usuario, new_Correo_usuario);
        return "Correo cambiado con exito!!!";
    }

    @Override
    public String changePasswordLogin(Long id_usuario, String old_password, String new_password) {
        this.repo.changePasswordLoginForId(id_usuario, new_password);
        return "Contraseña cambiada con exito!!!";
    }

    @Override
    public String deleteLogin(Long id_usuario) {
        // Usuario usuario = new Usuario();
        // usuario.setId_usuario(id_usuario);
        this.repo.deleteById(id_usuario);
        return "Login eliminado con exito!!!";
    }

}
