package com.sovereingschool.back_streaming.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_streaming.Models.Curso;
import com.sovereingschool.back_streaming.Models.Plan;
import com.sovereingschool.back_streaming.Models.RoleEnum;
import com.sovereingschool.back_streaming.Models.Usuario;

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
    RoleEnum findRollUsuarioForId(@Param("id") Long id);

    @Query("SELECT p FROM Plan p WHERE p.id_plan = (SELECT u.plan_usuario.id_plan FROM Usuario u WHERE u.id_usuario = :id)")
    Plan findPlanUsuarioForId(@Param("id") Long id);

    @Query("SELECT u.cursos_usuario FROM Usuario u WHERE u.id_usuario = :id ")
    List<Curso> findCursosUsuarioForId(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.nombre_usuario = :new_nombre_usuario WHERE u.id = :id")
    Integer changeNombreUsuarioForId(@Param("id") Long id, @Param("new_nombre_usuario") String new_nombre_usuario);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u = :usuario_modificado WHERE u.id = :id")
    Integer changeUsuarioForId(@Param("id") Long id, @Param("usuario_modificado") Usuario usuario_modificado);

    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.plan_usuario = :new_plan WHERE u.id_usuario = :id")
    Integer changePlanUsuarioForId(@Param("id") Long id, @Param("new_plan") Plan new_plan);

    @Query("SELECT u FROM Usuario u WHERE u.roll_usuario = RoleEnum.PROF OR u.roll_usuario = RoleEnum.ADMIN")
    List<Usuario> findProfes();

    @Query("SELECT u FROM Usuario u WHERE u.roll_usuario = RoleEnum.PROF OR u.roll_usuario = RoleEnum.ADMIN")
    List<Usuario> getInit();

    @Query("SELECT u FROM Usuario u WHERE u.nombre_usuario = :nombre_usuario")
    Optional<Usuario> findByNombreUsuario(String nombre_usuario);
}
