package com.sovereingschool.back_chat.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_chat.Models.Clase;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {

    @Query("SELECT c.nombre_clase FROM Clase c WHERE c.id_clase = :id")
    String findNombreClaseById(@Param("id") Long id);

}
