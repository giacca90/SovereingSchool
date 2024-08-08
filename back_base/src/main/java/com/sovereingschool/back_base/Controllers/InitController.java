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
        try {
            return new ResponseEntity<>(this.service.getInit(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
