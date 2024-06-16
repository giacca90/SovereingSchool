package com.sovereingschool.back.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sovereingschool.back.Interfaces.IUsuarioService;
import com.sovereingschool.back.Models.Curso;
import com.sovereingschool.back.Models.Plan;
import com.sovereingschool.back.Models.Usuario;
import com.sovereingschool.back.Repositories.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioService implements IUsuarioService {

    @Autowired
    private UsuarioRepository repo;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public String createUsuario(Usuario new_usuario) {
        this.repo.save(new_usuario);
        return "Usuario creado con exito!!!";
    }

    @Override
    public String getNombreUsuario(Long id_usuario) {
        return this.repo.findNombreUsuarioForId(id_usuario);
    }

    @Override
    public List<String> getFotosUsuario(Long id_usuario) {
        return this.repo.findFotosUsuarioForId(id_usuario);
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
        return this.repo.findCursosUsuarioForId(id_usuario);
    }

    @Override
    public Integer changeNombreUsuario(Usuario usuario) {
        return this.repo.changeNombreUsuarioForId(usuario.getId_usuario(), usuario.getNombre_usuario());
    }

    @Override
    public Integer changeFotosUsuario(Usuario usuario) {
        return this.repo.changeFotoUsuarioForId(usuario.getId_usuario(), usuario.getFoto_usuario());
    }

    @Override
    public Integer changePlanUsuario(Usuario usuario) {
        return this.repo.changePlanUsuarioForId(usuario.getId_usuario(), usuario.getPlan());
    }

    @Override
    public Integer changeCursosUsuario(Usuario usuario) {
        return this.repo.changeCursosUsuarioForId(usuario.getId_usuario(), usuario.getCursos());
    }

    @Override
    public String deleteUsuario(Long id) {
        this.repo.deleteById(id);
        return "Usuario eliminado con exito!!!";
    }

}
