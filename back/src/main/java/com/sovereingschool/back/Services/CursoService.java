package com.sovereingschool.back.Services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sovereingschool.back.Interfaces.ICursoService;
import com.sovereingschool.back.Models.Clase;
import com.sovereingschool.back.Models.Curso;
import com.sovereingschool.back.Models.Plan;
import com.sovereingschool.back.Repositories.CursoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class CursoService implements ICursoService {

    @Autowired
    private CursoRepository repo;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public String createCurso(Curso new_curso) {
        this.repo.save(new_curso);
        return "Nuevo curso creado correctamente!!!";
    }

    @Override
    public Curso getCurso(Long id_curso) {
        Optional<Curso> curso = this.repo.findById(id_curso);
        if (curso.isPresent()) {
            return curso.get();
        }
        return null;
    }

    @Override
    public String getNombreCurso(Long id_curso) {
        return this.repo.findNombreCursoById(id_curso);
    }

    @Override
    public List<String> getNombresProfesoresCurso(Long id_curso) {
        return this.repo.findNombresProfesoresCursoById(id_curso);
    }

    @Override
    public Date getFechaCreacionCurso(Long id_curso) {
        return this.repo.findFechaCreacionCursoById(id_curso);
    }

    @Override
    public List<Clase> getClasesDelCurso(Long id_curso) {
        return this.repo.findClasesCursoById(id_curso);
    }

    @Override
    public List<Plan> getPlanesDelCurso(Long id_curso) {
        return this.repo.findPlanesCursoById(id_curso);
    }

    @Override
    public BigDecimal getPrecioCurso(Long id_curso) {
        return this.repo.findPrecioCursoById(id_curso);
    }

    @Override
    public Curso updateCurso(Curso curso) {
        return this.repo.save(curso);
    }

    @Override
    public String deleteCurso(Long id_curso) {
        this.repo.deleteById(id_curso);
        return "Curso eliminado con exito!!!";
    }

}
