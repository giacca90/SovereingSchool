package com.sovereingschool.back_streaming.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sovereingschool.back_streaming.Models.Preset;
import com.sovereingschool.back_streaming.Models.Usuario;
import com.sovereingschool.back_streaming.Repositories.PresetRepository;
import com.sovereingschool.back_streaming.Repositories.UsuarioRepository;

@RestController
public class PresetController {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PresetRepository presetRepository;

    @GetMapping("/prueba")
    public ResponseEntity<Boolean> getMethodName() {
        try {
            this.usuarioRepository.findAll().forEach((Usuario user) -> {
                if (user.getRoll_usuario() < 2) {
                    System.out.println("Usuario: " + user.getId_usuario());
                    this.presetRepository.save(new Preset(user.getId_usuario()));
                }
            });
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error en crear DDBB de presets: " + e.getMessage());
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
