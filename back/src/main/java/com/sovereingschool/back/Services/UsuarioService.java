package com.sovereingschool.back.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sovereingschool.back.DTOs.NewUsuario;
import com.sovereingschool.back.Interfaces.IUsuarioService;
import com.sovereingschool.back.Models.Curso;
import com.sovereingschool.back.Models.Login;
import com.sovereingschool.back.Models.Plan;
import com.sovereingschool.back.Models.Usuario;
import com.sovereingschool.back.Repositories.LoginRepository;
import com.sovereingschool.back.Repositories.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioService implements IUsuarioService {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private LoginRepository loginRepo;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public String createUsuario(NewUsuario new_usuario) {
        Usuario usuario = new Usuario();
        usuario.setNombre_usuario(new_usuario.getNombre_usuario());
        usuario.setFoto_usuario(new_usuario.getFoto_usuario());
        usuario.setRoll_usuario(new_usuario.getRoll_usuario());
        usuario.setPlan_usuario(new_usuario.getPlan_usuario());
        usuario.setCursos_usuario(new_usuario.getCursos_usuario());
        usuario.setFecha_registro_usuario(new_usuario.getFecha_registro_usuario());
        Usuario usuarioInsertado = this.repo.save(usuario);
        if (usuarioInsertado.getId_usuario() == null)
            return "Error en crear el usuario";
        Login login = new Login();
        // login.setId_usuario(usuarioInsertado.getId_usuario());
        login.setUsuario(usuarioInsertado);
        login.setCorreo_electronico(new_usuario.getCorreo_electronico());
        login.setPassword(new_usuario.getPassword());
        this.loginRepo.save(login);
        return "Usuario creado con exito!!!";
    }

    @Override
    public Usuario getUsuario(Long id_usuario) {
        return this.repo.findUsuarioForId(id_usuario);
    }

    @Override
    public String getNombreUsuario(Long id_usuario) {
        return this.repo.findNombreUsuarioForId(id_usuario);
    }

    @Override
    public List<String> getFotosUsuario(Long id_usuario) {
        Usuario usuario = this.repo.findUsuarioForId(id_usuario);
        if (usuario == null)
            return null;
        return usuario.getFoto_usuario();
    }

    @Override
    public Integer getRollUsuario(Long id_usuario) {
        return this.repo.findRollUsuarioForId(id_usuario);
    }

    @Override
    public Plan getPlanUsuario(Long id_usuario) {
        return this.repo.findPlanUsuarioForId(id_usuario);
    }

    @Override
    public List<Curso> getCursosUsuario(Long id_usuario) {
        Usuario usuario = this.repo.findUsuarioForId(id_usuario);
        if (usuario == null)
            return null;
        return usuario.getCursos();
    }

    @Override
    public Integer changeNombreUsuario(Usuario usuario) {
        return this.repo.changeNombreUsuarioForId(usuario.getId_usuario(), usuario.getNombre_usuario());
    }

    @Override
    public Integer changeFotosUsuario(Usuario usuario) {
        Usuario old_usuario = this.repo.findUsuarioForId(usuario.getId_usuario());
        if (old_usuario == null)
            return 0;
        old_usuario.setFoto_usuario(usuario.getFoto_usuario());
        return this.repo.changeUsuarioForId(usuario.getId_usuario(), old_usuario);
    }

    @Override
    public Integer changePlanUsuario(Usuario usuario) {
        return this.repo.changePlanUsuarioForId(usuario.getId_usuario(), usuario.getPlan());
    }

    @Override
    public Integer changeCursosUsuario(Usuario usuario) {
        Usuario old_usuario = this.repo.findUsuarioForId(usuario.getId_usuario());
        if (old_usuario == null)
            return 0;
        old_usuario.setCursos_usuario(usuario.getCursos_usuario());
        return this.repo.changeUsuarioForId(usuario.getId_usuario(), old_usuario);
    }

    @Override
    public String deleteUsuario(Long id) {
        if (this.repo.findUsuarioForId(id) == null) {
            return null;
        }
        this.loginRepo.deleteById(id);
        this.repo.deleteById(id);
        return "Usuario eliminado con exito!!!";
    }

}
