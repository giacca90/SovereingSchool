package com.sovereingschool.back_chat.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_chat.Models.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

}
