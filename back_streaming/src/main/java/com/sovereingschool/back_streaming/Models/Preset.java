package com.sovereingschool.back_streaming.Models;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "presets")
@Data
public class Preset {
    @Data
    public static class PresetValue {
        private List<VideoElement> elements;
        private String shortcut;
    }

    @Data
    public static class VideoElement {
        private String id;
        private Object element;
        private boolean painted;
        private double scale;
        private Position position;
    }

    @Data
    public static class Position {
        private int x;
        private int y;
    }

    @Id
    private String id;
    private Long id_usuario;
    private Map<String, PresetValue> presets;

    public Preset(Long id_usuario) {
        this.id_usuario = id_usuario;
    }

    public Preset(Long id_usuario, Map<String, PresetValue> presets) {
        this.id_usuario = id_usuario;
        this.presets = presets;
    }
}
