package com.sovereingschool.back_base.Services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sovereingschool.back_base.DTOs.CursosInit;
import com.sovereingschool.back_base.DTOs.Estadistica;
import com.sovereingschool.back_base.DTOs.InitApp;
import com.sovereingschool.back_base.DTOs.ProfesInit;
import com.sovereingschool.back_base.Interfaces.IInitAppService;
import com.sovereingschool.back_base.Models.Curso;
import com.sovereingschool.back_base.Models.Usuario;
import com.sovereingschool.back_base.Repositories.ClaseRepository;
import com.sovereingschool.back_base.Repositories.CursoRepository;
import com.sovereingschool.back_base.Repositories.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class InitAppService implements IInitAppService {

    @Autowired
    private CursoRepository cursoRepo;

    @Autowired
    private ClaseRepository claseRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Override
    public List<Usuario> getProfesores() {
        return this.usuarioRepo.getInit();
    }

    @Override
    public InitApp getInit() {
        List<Usuario> profes = this.usuarioRepo.getInit();
        List<ProfesInit> profesInit = new ArrayList<>();
        profes.forEach(profe -> {
            ProfesInit init = new ProfesInit();
            init.setId_usuario(profe.getId_usuario());
            init.setNombre_usuario(profe.getNombre_usuario());
            init.setFoto_usuario(profe.getFoto_usuario());
            init.setPresentacion(profe.getPresentacion());
            profesInit.add(init);

        });

        List<Curso> cursos = this.cursoRepo.getAllCursos();
        List<CursosInit> cursosInit = new ArrayList<>();
        cursos.forEach(curso -> {
            CursosInit init = new CursosInit();
            init.setId_curso(curso.getId_curso());
            init.setNombre_curso(curso.getNombre_curso());
            init.setImagen_curso(curso.getImagen_curso());
            init.setDescriccion_corta(curso.getDescriccion_corta());
            List<Long> ids_profes = new ArrayList<>();
            curso.getProfesores_curso().forEach(profe -> {
                ids_profes.add(profe.getId_usuario());
            });

            init.setProfesores_curso(ids_profes);
            cursosInit.add(init);
        });

        Estadistica estadistica = new Estadistica();
        estadistica.setClases(this.claseRepo.count());
        estadistica.setCursos(this.cursoRepo.count());
        estadistica.setProfesores(profesInit.size());
        estadistica.setAlumnos(this.usuarioRepo.count() - profesInit.size());

        InitApp init = new InitApp();
        init.setCursosInit(cursosInit);
        init.setProfesInit(profesInit);
        init.setEstadistica(estadistica);

        return init;

    }

}
