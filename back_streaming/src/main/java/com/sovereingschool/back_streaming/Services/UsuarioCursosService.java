package com.sovereingschool.back_streaming.Services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sovereingschool.back_streaming.Interfaces.IUsuarioCursos;
import com.sovereingschool.back_streaming.Models.Clase;
import com.sovereingschool.back_streaming.Models.Curso;
import com.sovereingschool.back_streaming.Models.StatusClase;
import com.sovereingschool.back_streaming.Models.StatusCurso;
import com.sovereingschool.back_streaming.Models.Usuario;
import com.sovereingschool.back_streaming.Models.UsuarioCursos;
import com.sovereingschool.back_streaming.Repositories.UsuarioCursosRepository;
import com.sovereingschool.back_streaming.Repositories.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioCursosService implements IUsuarioCursos {

    @Autowired
    private UsuarioRepository usuarioRepository; // Repositorio de PostgreSQL para usuarios

    @Autowired
    private UsuarioCursosRepository usuarioCursosRepository; // Repositorio de MongoDB

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
            userCourses.setCursos(courseStatuses);

            usuarioCursosRepository.save(userCourses);
        }
    }
}
