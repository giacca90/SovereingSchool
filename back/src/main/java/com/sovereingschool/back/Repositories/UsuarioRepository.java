package com.sovereingschool.back.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back.Models.Curso;
import com.sovereingschool.back.Models.Plan;
import com.sovereingschool.back.Models.Usuario;

import jakarta.transaction.Transactional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u WHERE u.id_usuario = :id")
    Usuario findUsuarioForId(@Param("id") Long id);

    @Query("SELECT u.nombre_usuario FROM Usuario u WHERE u.id_usuario = :id")
    String findNombreUsuarioForId(@Param("id") Long id);

    @Query("SELECT u.foto_usuario FROM Usuario u WHERE u.id_usuario = :id")
    List<String> findFotosUsuarioForId(@Param("id") Long id);

    @Query("SELECT u.roll_usuario FROM Usuario u WHERE u.id_usuario = :id")
    Integer findRollUsuarioForId(@Param("id") Long id);

    @Query("SELECT p FROM Plan p WHERE p.id_plan = (SELECT u.plan.id_plan FROM Usuario u WHERE u.id_usuario = :id)")
    Plan findPlanUsuarioForId(@Param("id") Long id);

    @Query("SELECT u.cursos FROM Usuario u WHERE u.id_usuario = :id ")
    List<Curso> findCursosUsuarioForId(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.nombre_usuario = :new_nombre_usuario WHERE u.id = :id")
    Integer changeNombreUsuarioForId(@Param("id") Long id, @Param("new_nombre_usuario") String new_nombre_usuario);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.foto_usuario = :new_fotos_usuario WHERE u.id = :id")
    Integer changeFotoUsuarioForId(@Param("id") Long id, @Param("new_fotos_usuario") List<String> new_fotos_usuario);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.plan = :new_plan WHERE u.id_usuario = :id")
    Integer changePlanUsuarioForId(@Param("id") Long id, @Param("new_plan") Plan new_plan);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.cursos = :new_cursos WHERE u.id_usuario = :id")
    Integer changeCursosUsuarioForId(@Param("id") Long id, @Param("new_cursos") List<Curso> new_cursos);

}
