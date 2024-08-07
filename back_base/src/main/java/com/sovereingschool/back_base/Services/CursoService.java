package com.sovereingschool.back_base.Services;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sovereingschool.back_base.Interfaces.ICursoService;
import com.sovereingschool.back_base.Models.Clase;
import com.sovereingschool.back_base.Models.Curso;
import com.sovereingschool.back_base.Models.Plan;
import com.sovereingschool.back_base.Models.Usuario;
import com.sovereingschool.back_base.Repositories.ClaseRepository;
import com.sovereingschool.back_base.Repositories.CursoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class CursoService implements ICursoService {

    @Autowired
    private CursoRepository repo;

    @Autowired
    private ClaseRepository claseRepo;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Long createCurso(Curso new_curso) {
        new_curso.setId_curso(null);
        Curso res = this.repo.save(new_curso);
        return res.getId_curso();
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
    public List<Usuario> getProfesoresCurso(Long id_curso) {
        return this.repo.findProfesoresCursoById(id_curso);
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
        Optional<Curso> respuesta = this.repo.findById(curso.getId_curso());
        if (!respuesta.isPresent())
            return null;
        Curso oldCurso = respuesta.get();
        for (Clase clase : curso.getClases_curso()) {
            if (!oldCurso.getClases_curso().contains(clase)) {
                if (clase.getId_clase() == 0) {
                    clase = this.claseRepo.save(clase);
                } else {
                    this.claseRepo.updateClase(clase.getId_clase(), clase.getNombre_clase(), clase.getTipo_clase(),
                            clase.getDireccion_clase(), clase.getPosicion_clase());
                }
            }
        }
        return this.repo.save(curso);
    }

    @Override
    public String deleteCurso(Long id_curso) {
        if (!this.repo.findById(id_curso).isPresent())
            return null;
        this.repo.deleteById(id_curso);
        return "Curso eliminado con éxito!!!";
    }

    @Override
    public List<Curso> getAll() {
        return this.repo.findAll();
    }

    @Override
    public void deleteClase(Clase clase) {
        Optional<Clase> optionalClase = this.claseRepo.findById(clase.getId_clase());
        if (optionalClase.isPresent()) {
            this.claseRepo.delete(optionalClase.get());
            Path path = Paths.get(clase.getDireccion_clase());
            try {

                if (Files.exists(path)) {
                    Files.delete(path);
                }
            } catch (Exception e) {
                System.err.println("Error en borrar el video: " + e.getMessage());
            }
        } else {
            System.err.println("Clase no encontrada con ID: " + clase.getId_clase());
        }
    }

}
