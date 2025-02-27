package com.sovereingschool.back_streaming.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sovereingschool.back_streaming.Services.UsuarioPresetsService;

@RestController
public class PresetController {

    @Autowired
    private UsuarioPresetsService usuarioPresetsService;

    @GetMapping("/prueba")
    public ResponseEntity<Boolean> getMethodName() {
        try {
            Boolean res = this.usuarioPresetsService.createPresetsForEligibleUsers();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error en crear DDBB de presets: " + e.getMessage());
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
