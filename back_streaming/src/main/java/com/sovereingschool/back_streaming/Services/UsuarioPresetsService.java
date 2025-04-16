package com.sovereingschool.back_streaming.Services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sovereingschool.back_streaming.Models.Preset;
import com.sovereingschool.back_streaming.Models.Usuario;
import com.sovereingschool.back_streaming.Repositories.PresetRepository;
import com.sovereingschool.back_streaming.Repositories.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioPresetsService {
    @Autowired
    private PresetRepository presetRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public boolean createPresetsForEligibleUsers() {
        try {
            this.usuarioRepository.findAll().forEach((Usuario user) -> {
                if (user.getRoll_usuario().name().equals("PROFESOR")
                        || user.getRoll_usuario().name().equals("ADMIN")) {
                    this.presetRepository.save(new Preset(user.getId_usuario()));
                }
            });
            return true;
        } catch (Exception e) {
            System.err.println("Error en crear DDBB de presets: " + e.getMessage());
            return false;
        }
    }

    public Preset getPresetsForUser(Long id_usuario) {
        return this.presetRepository.findByIdUsuario(id_usuario);
    }

    public void savePresetsForUser(Long id_usuario, Map<String, Preset.PresetValue> presets) {
        this.deletePresetsForUser(id_usuario);
        this.presetRepository.save(new Preset(id_usuario, presets));
    }

    public void deletePresetsForUser(Long id_usuario) {
        this.presetRepository.deleteByIdUsuario(id_usuario);
    }

}
