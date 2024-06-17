package com.sovereingschool.back.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sovereingschool.back.DTOs.ChangePassword;
import com.sovereingschool.back.Interfaces.ILoginService;
import com.sovereingschool.back.Models.Login;
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
    public String createNuevoLogin(Login login) {
        this.repo.save(login);
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
    public String changeCorreoLogin(Login login) {

        this.repo.changeCorreoLoginForId(login.getId_usuario(), login.getCorreo_electronico());
        return "Correo cambiado con exito!!!";
    }

    @Override
    public String changePasswordLogin(ChangePassword changepassword) {
        if (this.repo.findPasswordLoginForId(changepassword.getId_usuario()) == changepassword.getOld_password()) {
            this.repo.changePasswordLoginForId(changepassword.getId_usuario(), changepassword.getNew_password());
            return "Contraseña cambiada con exito!!!";
        }
        return "La contraseña antigua no es correcta!!!";
    }

    @Override
    public String deleteLogin(Long id_usuario) {
        // Usuario usuario = new Usuario();
        // usuario.setId_usuario(id_usuario);
        this.repo.deleteById(id_usuario);
        return "Login eliminado con exito!!!";
    }

}
