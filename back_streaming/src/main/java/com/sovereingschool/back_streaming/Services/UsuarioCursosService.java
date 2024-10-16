package com.sovereingschool.back_streaming.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.sovereingschool.back_streaming.Interfaces.IUsuarioCursosService;
import com.sovereingschool.back_streaming.Models.Clase;
import com.sovereingschool.back_streaming.Models.Curso;
import com.sovereingschool.back_streaming.Models.StatusClase;
import com.sovereingschool.back_streaming.Models.StatusCurso;
import com.sovereingschool.back_streaming.Models.Usuario;
import com.sovereingschool.back_streaming.Models.UsuarioCursos;
import com.sovereingschool.back_streaming.Repositories.ClaseRepository;
import com.sovereingschool.back_streaming.Repositories.CursoRepository;
import com.sovereingschool.back_streaming.Repositories.UsuarioCursosRepository;
import com.sovereingschool.back_streaming.Repositories.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioCursosService implements IUsuarioCursosService {

    @Autowired
    private UsuarioRepository usuarioRepository; // Repositorio de PostgreSQL para usuarios

    @Autowired
    private CursoRepository cursoRepository; // Repositorio de PostgreSQL para clases

    @Autowired
    private ClaseRepository claseRepository; // Repositorio de PostgreSQL para clases

    @Autowired
    private UsuarioCursosRepository usuarioCursosRepository; // Repositorio de MongoDB

    @Autowired
    private MongoTemplate mongoTemplate; // MongoDB Template

    @Override
    public void syncUserCourses() {
        List<Usuario> users = usuarioRepository.findAll();
        for (Usuario user : users) {
            List<Curso> courses = user.getCursos_usuario();
            List<StatusCurso> courseStatuses = courses.stream().map(course -> {
                List<Clase> classes = course.getClases_curso();
                List<StatusClase> classStatuses = classes.stream().map(clazz -> {
                    StatusClase classStatus = new StatusClase();
                    classStatus.setId_clase(clazz.getId_clase());
                    classStatus.setCompleted(false);
                    classStatus.setProgress(0);
                    return classStatus;
                }).collect(Collectors.toList());

                StatusCurso courseStatus = new StatusCurso();
                courseStatus.setId_curso(course.getId_curso());
                courseStatus.setClases(classStatuses);
                return courseStatus;
            }).collect(Collectors.toList());

            UsuarioCursos userCourses = new UsuarioCursos();
            userCourses.setId_usuario(user.getId_usuario());
            userCourses.setRol_usuario(user.getRoll_usuario());
            userCourses.setCursos(courseStatuses);
            usuarioCursosRepository.save(userCourses);
        }
    }

    @Override
    public String addNuevoUsuario(Usuario usuario) {
        List<Curso> courses = usuario.getCursos_usuario();
        List<StatusCurso> courseStatuses = new ArrayList<>();
        if (courses != null) {
            courseStatuses = courses.stream().map(course -> {
                List<Clase> classes = course.getClases_curso();
                List<StatusClase> classStatuses = classes.stream().map(clazz -> {
                    StatusClase classStatus = new StatusClase();
                    classStatus.setId_clase(clazz.getId_clase());
                    classStatus.setCompleted(false);
                    classStatus.setProgress(0);
                    return classStatus;
                }).collect(Collectors.toList());

                StatusCurso courseStatus = new StatusCurso();
                courseStatus.setId_curso(course.getId_curso());
                courseStatus.setClases(classStatuses);
                return courseStatus;
            }).collect(Collectors.toList());
        }

        UsuarioCursos userCourses = new UsuarioCursos();
        userCourses.setId_usuario(usuario.getId_usuario());
        userCourses.setCursos(courseStatuses);

        usuarioCursosRepository.save(userCourses);
        return "Nuevo Usuario Insertado con Exito!!!";
    }

    @Override
    public String addCursoUsuario(Usuario usuario) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addCursoUsuario'");
    }

    @Override
    public String getClase(Long id_usuario, Long id_curso, Long id_clase) {
        System.out.println("IDUsuario: " + id_usuario);
        UsuarioCursos usuario = this.usuarioCursosRepository.findByIdUsuario(id_usuario);
        if (usuario == null)
            return null;

        if (id_clase == 0) {
            if (usuario.getRol_usuario() < 2) {
                id_clase = this.cursoRepository.findById(id_curso).get().getClases_curso().get(0).getId_clase();
            }
        }

        if (usuario.getRol_usuario() < 2) {
            return this.claseRepository.findById(id_clase).get().getDireccion_clase();
        }

        List<StatusCurso> cursos = usuario.getCursos();

        for (StatusCurso curso : cursos) {
            if (curso.getId_curso().equals(id_curso)) {
                List<StatusClase> clases = curso.getClases();
                for (StatusClase clase : clases) {
                    if (id_clase == 0) {
                        if (!clase.isCompleted()) {
                            return this.claseRepository.findById(clase.getId_clase()).get().getDireccion_clase();
                        }
                        continue;
                    }
                    if (clase.getId_clase().equals(id_clase)) {
                        return this.claseRepository.findById(id_clase).get().getDireccion_clase();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean addClase(Long idCurso, Clase clase) {
        try {
            // Encuentra el documento que contiene el curso específico
            Query query = new Query();
            query.addCriteria(Criteria.where("cursos.id_curso").is(idCurso));
            List<UsuarioCursos> usuarioCursos = mongoTemplate.find(query, UsuarioCursos.class);

            if (usuarioCursos == null || usuarioCursos.size() == 0) {
                System.err.println("No se encontró el documento.");
                return false;
            }

            for (UsuarioCursos usuario : usuarioCursos) {
                for (StatusCurso curso : usuario.getCursos()) {
                    if (curso.getId_curso().equals(idCurso)) {
                        curso.getClases()
                                .add(new StatusClase(clase.getId_clase(), false, 0));
                        mongoTemplate.save(usuario);
                        break;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error en añadir la cueva clase: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteClase(Long idCurso, Long idClase) {
        try {
            // Encuentra el documento que contiene el curso específico
            Query query = new Query();
            query.addCriteria(Criteria.where("cursos.id_curso").is(idCurso));
            List<UsuarioCursos> usuarioCursos = mongoTemplate.find(query, UsuarioCursos.class);

            if (usuarioCursos == null || usuarioCursos.size() == 0) {
                System.err.println("No se encontró el documento.");
                return false;
            }

            for (UsuarioCursos usuario : usuarioCursos) {
                for (StatusCurso curso : usuario.getCursos()) {
                    if (curso.getId_curso().equals(idCurso)) {
                        for (int i = 0; i < curso.getClases().size(); i++) {
                            if (curso.getClases().get(i).getId_clase().equals(idClase)) {
                                curso.getClases().remove(i);
                                mongoTemplate.save(usuario);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error en borrar la clase: " + e.getMessage());
            return false;
        }
    }

    public Long getStatus(Long id_usuario, Long id_curso) {
        UsuarioCursos uc = this.usuarioCursosRepository.findByIdUsuario(id_usuario);
        if (uc != null) {
            if (uc.getRol_usuario() < 2) {
                return this.cursoRepository.findById(id_curso).get().getClases_curso().get(0).getId_clase();
            } else {
                List<StatusCurso> lst = uc.getCursos();
                for (StatusCurso sc : lst) {
                    if (sc.getId_curso().equals(id_curso)) {
                        List<StatusClase> lscl = sc.getClases();
                        for (StatusClase scl : lscl) {
                            if (!scl.isCompleted()) {
                                return scl.getId_clase();
                            }
                        }
                        return lscl.get(0).getId_clase();
                    }
                }
            }
        }
        return 0L;
    }

    public boolean deleteCurso(Long id) {
        // Encuentra el documento que contiene el curso específico
        Query query = new Query();
        query.addCriteria(Criteria.where("cursos.id_curso").is(id));
        List<UsuarioCursos> usuarioCursos = mongoTemplate.find(query, UsuarioCursos.class);

        if (usuarioCursos == null || usuarioCursos.size() == 0) {
            System.err.println("No se encontró el documento.");
            return false;
        }

        for (UsuarioCursos UC : usuarioCursos) {
            List<StatusCurso> statusCurso = UC.getCursos();
            for (int i = 0; i < statusCurso.size(); i++) {
                if (statusCurso.get(i).getId_curso().equals(id)) {
                    statusCurso.remove(i);
                    this.usuarioCursosRepository.save(UC);
                    break;
                }
            }
        }
        return true;
    }
}
