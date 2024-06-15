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
    public String createUsuario(String nombre_usuario, List<String> fotos_usuario, Plan plan_usuario,
            List<Curso> cursos_usuario) {
        Usuario usuario = new Usuario();
        usuario.setNombre_usuario(nombre_usuario);
        usuario.setFoto_usuario(fotos_usuario);
        usuario.setPlan_usuario(plan_usuario);
        usuario.setCursos_usuario(cursos_usuario);

        this.repo.save(usuario);
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
    public int getRollUsuario(Long id_usuario) {
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
    public String changeNombreUsuario(Long id_usuario, String new_nombre_usuario) {
        return this.repo.changeNombreUsuarioForId(id_usuario, new_nombre_usuario);
    }

    @Override
    public String changeFotosUsuario(Long id_usuario, List<String> fotos_usuario) {
        return this.repo.changeFotoUsuarioForId(id_usuario, fotos_usuario);
    }

    @Override
    public String changePlanUsuario(Long id_usuario, Plan plan) {
        return this.repo.changePlanUsuarioForId(id_usuario, plan);
    }

    @Override
    public String changeCursosUsuario(Long id_usuario, List<Curso> cursos_usuario) {
        return this.repo.changeCursosUsuarioForId(id_usuario, cursos_usuario);
    }

    @Override
    public String deleteUsuario(Long id_usuario) {
        this.repo.deleteById(id_usuario);
        return "Usuario eliminado con exito!!!";
    }

}
