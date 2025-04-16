package com.sovereingschool.back_base.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sovereingschool.back_base.Interfaces.IInitAppService;

@RestController
@PreAuthorize("hasAnyRole('GUEST', 'USER', 'PROF', 'ADMIN')")
@RequestMapping("/init")
public class InitController {

    private static final Logger logger = LoggerFactory.getLogger(InitController.class);

    @Autowired
    private IInitAppService service;

    @GetMapping()
    public ResponseEntity<?> get() {
        Object response = new Object();
        try {
            logger.info("Intentando acceder al endpoint init con los roles del usuario actual");
            response = this.service.getInit();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error en la carga inicial: " + e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
