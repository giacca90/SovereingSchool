package com.sovereingschool.back_chat.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_chat.Models.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT u.nombre_usuario FROM Usuario u WHERE u.id_usuario = :id")
    String findNombreUsuarioForId(@Param("id") Long id);

    @Query("SELECT u.foto_usuario FROM Usuario u WHERE u.id_usuario = :id")
    List<String> findFotosUsuarioForId(@Param("id") Long id);

}
