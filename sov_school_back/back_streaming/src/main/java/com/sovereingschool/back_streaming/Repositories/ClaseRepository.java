package com.sovereingschool.back_streaming.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sovereingschool.back_streaming.Models.Clase;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {

}
