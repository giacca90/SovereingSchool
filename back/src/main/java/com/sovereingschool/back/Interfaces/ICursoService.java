package com.sovereingschool.back.Interfaces;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.sovereingschool.back.Models.Clase;
import com.sovereingschool.back.Models.Curso;
import com.sovereingschool.back.Models.Plan;

public interface ICursoService {
    public String createCurso(Curso new_curso);

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
