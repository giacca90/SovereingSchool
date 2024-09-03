package com.sovereingschool.back_base.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sovereingschool.back_base.Interfaces.IInitAppService;

@RestController
@RequestMapping("/init")
@CrossOrigin(origins = "http://localhost:4200, https://giacca90.github.io")
public class InitController {

    @Autowired
    private IInitAppService service;

    @GetMapping()
    public ResponseEntity<?> get() {
        Object response = new Object();
        try {
            response = this.service.getInit();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response = "Error en la carga inicial: " + e.getMessage();
            return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
