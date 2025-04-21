package com.sovereingschool.back_base.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
    public ResponseEntity<?> getInit() {
        Object response = new Object();
        try {
            response = this.service.getInit();
            String initToken = this.service.getInitToken();
            // Construir la cookie segura
            ResponseCookie initTokenCookie = ResponseCookie.from("initToken", initToken)
                    .httpOnly(true) // No accesible desde JavaScript
                    .secure(true) // Solo por HTTPS
                    .path("/") // Ruta donde será accesible
                    .sameSite("None") // Cambia a "None" si trabajas con frontend separado
                    .build();

            return ResponseEntity.ok()
                    .header("Set-Cookie", initTokenCookie.toString())
                    .body(response);
        } catch (Exception e) {
            logger.error("Error en la carga inicial: " + e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
