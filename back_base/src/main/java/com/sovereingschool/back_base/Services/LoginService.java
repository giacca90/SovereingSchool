package com.sovereingschool.back_base.Services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sovereingschool.back_base.DTOs.ChangePassword;
import com.sovereingschool.back_base.Interfaces.ILoginService;
import com.sovereingschool.back_base.Models.Login;
import com.sovereingschool.back_base.Repositories.LoginRepository;

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
    public Long compruebaCorreo(String correo) {
        Long result = this.repo.compruebaCorreo(correo);
        if (result == null)
            return 0L;
        return result;
    }

    @Override
    public String createNuevoLogin(Login login) {
        this.repo.save(login);
        return "Nuevo Usuario creado con éxito!!!";
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
        return "Correo cambiado con éxito!!!";
    }

    @Override
    public Integer changePasswordLogin(ChangePassword changepassword) {
        if (changepassword.getNew_password().length() < 1 || changepassword.getOld_password().length() < 1)
            return null;
        if (this.repo.findPasswordLoginForId(changepassword.getId_usuario()).equals(changepassword.getOld_password())) {
            this.repo.changePasswordLoginForId(changepassword.getId_usuario(), changepassword.getNew_password());
            return 1;
        }
        return 0;
    }

    @Override
    public String deleteLogin(Long id_usuario) {
        this.repo.deleteById(id_usuario);
        return "Login eliminado con éxito!!!";
    }

    @Override
    public Login getLogin(Long id) {
        Optional<Login> log = this.repo.findById(id);
        if (log.isPresent())
            return log.get();
        return null;
    }

}
