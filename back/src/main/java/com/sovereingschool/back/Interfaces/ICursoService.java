package com.sovereingschool.back.Interfaces;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.sovereingschool.back.Models.Clase;
import com.sovereingschool.back.Models.Curso;
import com.sovereingschool.back.Models.Plan;
import com.sovereingschool.back.Models.Usuario;

public interface ICursoService {
    public String createCurso(String nombre_curso, List<Usuario> profesores_curso, List<Clase> clases_del_curso,
            List<Plan> planes_del_curso, BigDecimal precio_curso);

    public Curso getCurso(Long id_curso);

    public String getNombreCurso(Long id_curso);

    public List<String> getNombresProfesoresCurso(Long id_curso);

    public Date getFechaCreacionCurso(Long id_curso);

    public List<Clase> getClasesDelCurso(Long id_curso);

    public List<Plan> getPlanesDelCurso(Long id_curso);

    public BigDecimal getPrecioCurso(Long id_curso);

    public Curso updateCurso(Curso curso);

    public String deleteCurso(Long id_curso);

}
