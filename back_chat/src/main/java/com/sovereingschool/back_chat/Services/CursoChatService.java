package com.sovereingschool.back_chat.Services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sovereingschool.back_chat.DTOs.ClaseChatDTO;
import com.sovereingschool.back_chat.DTOs.CursoChatDTO;
import com.sovereingschool.back_chat.DTOs.MensajeChatDTO;
import com.sovereingschool.back_chat.Models.Clase;
import com.sovereingschool.back_chat.Models.ClaseChat;
import com.sovereingschool.back_chat.Models.Curso;
import com.sovereingschool.back_chat.Models.CursoChat;
import com.sovereingschool.back_chat.Models.MensajeChat;
import com.sovereingschool.back_chat.Models.Usuario;
import com.sovereingschool.back_chat.Models.UsuarioChat;
import com.sovereingschool.back_chat.Repositories.ClaseRepository;
import com.sovereingschool.back_chat.Repositories.CursoChatRepository;
import com.sovereingschool.back_chat.Repositories.CursoRepository;
import com.sovereingschool.back_chat.Repositories.MensajeChatRepository;
import com.sovereingschool.back_chat.Repositories.UsuarioChatRepository;
import com.sovereingschool.back_chat.Repositories.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CursoChatService {

    @Autowired
    private CursoChatRepository cursoChatRepo;

    @Autowired
    private InitChatService initChatService;

    @Autowired
    private CursoRepository cursoRepo;

    @Autowired
    private ClaseRepository claseRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private MensajeChatRepository mensajeChatRepo;

    @Autowired
    private UsuarioChatRepository usuarioChatRepo;

    public CursoChatDTO getChat(Long idCurso) {
        CursoChat cursoChat = cursoChatRepo.findByIdCurso(idCurso);
        // ("LOG1: " + cursoChat.toString());
        CursoChatDTO cursoChatDTO = null;
        if (cursoChat != null) {
            List<ClaseChat> clases = cursoChat.getClases();
            List<ClaseChatDTO> clasesDTO = new ArrayList<>();
            // ("LOG2: " + clases.toString());
            if (clases != null && clases.size() > 0) {
                for (ClaseChat clase : clases) {
                    List<String> mensajes = clase.getMensajes();
                    // ("LOG3: " + mensajes.toString());
                    List<MensajeChatDTO> mensajesDTO = new ArrayList<>();
                    if (mensajes != null && mensajes.size() > 0) {
                        List<MensajeChat> mensajesChat = mensajeChatRepo.findAllById(mensajes);
                        if (mensajesChat != null && mensajesChat.size() > 0) {
                            mensajesDTO = initChatService.getMensajesDTO(mensajesChat);
                        }
                    }
                    clasesDTO.add(new ClaseChatDTO(
                            clase.getIdClase(),
                            cursoChat.getIdCurso(),
                            claseRepo.findNombreClaseById(clase.getIdClase()),
                            mensajesDTO));
                }
            }

            List<String> mensajes = cursoChat.getMensajes();
            List<MensajeChatDTO> mensajesDTO = new ArrayList<>();
            // ("LOG4: " + mensajes.toString());
            if (mensajes != null && mensajes.size() > 0) {
                List<MensajeChat> mensajesChat = mensajeChatRepo.findAllById(mensajes);
                if (mensajesChat != null && mensajesChat.size() > 0) {
                    mensajesDTO = initChatService.getMensajesDTO(mensajesChat);
                }
            }

            cursoChatDTO = new CursoChatDTO(
                    idCurso,
                    clasesDTO,
                    mensajesDTO,
                    cursoRepo.findNombreCursoById(idCurso),
                    cursoRepo.findImagenCursoById(idCurso));
        }

        // ("LOG5: " + cursoChatDTO.toString());
        return cursoChatDTO;
    }

    public void init() {
        List<Curso> cursos = cursoRepo.findAll();
        for (Curso curso : cursos) {
            List<Clase> clases = curso.getClases_curso();
            List<ClaseChat> clasesChat = new ArrayList<>();
            for (Clase clase : clases) {
                ClaseChat claseChat = new ClaseChat(clase.getId_clase(), curso.getId_curso(), new ArrayList<>());
                clasesChat.add(claseChat);
            }
            CursoChat cursoChat = new CursoChat(null, curso.getId_curso(), clasesChat, new ArrayList<>(), null);
            cursoChatRepo.save(cursoChat);
        }

        List<Usuario> usuarios = usuarioRepo.findAll();
        for (Usuario usuario : usuarios) {
            UsuarioChat usuarioChat = new UsuarioChat(null, usuario.getId_usuario(), new ArrayList<>(),
                    new ArrayList<>());
            usuarioChatRepo.save(usuarioChat);
        }
    }

    public void guardaMensaje(String message) {
        // Crear una instancia de ObjectMapper para parsear el JSON
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Convertir el JSON en un objeto MensajeChatDTO
            MensajeChatDTO mensajeChatDTO = objectMapper.readValue(message, MensajeChatDTO.class);

            // Aquí puedes agregar la lógica para guardar el mensaje en la base de datos
            String resp = null;
            if (mensajeChatDTO.getRespuesta() != null) {
                resp = mensajeChatDTO.getRespuesta().getId_mensaje();
            }
            MensajeChat mensajeChat = new MensajeChat(
                    null, // String id
                    mensajeChatDTO.getId_curso(), // Long id_curso
                    mensajeChatDTO.getId_clase(), // Long id_clase
                    mensajeChatDTO.getId_usuario(), // Long id_usuario
                    resp, // String respuesta
                    mensajeChatDTO.getPregunta(), // int momento
                    mensajeChatDTO.getMensaje(), // String mensaje
                    mensajeChatDTO.getFecha()); // Date fecha
            MensajeChat mex = this.mensajeChatRepo.save(mensajeChat);
            // ("MEX: " + mex);

            String idMex = mex.getId();

            // Actualizar el estado del curso
            CursoChat cursoChat = cursoChatRepo.findByIdCurso(mensajeChatDTO.getId_curso());
            if (mensajeChatDTO.getId_clase() == 0) {
                cursoChat.getMensajes().add(idMex);
            } else {
                List<ClaseChat> claseChatList = cursoChat.getClases();
                claseChatList.forEach(claseChat -> {
                    if (claseChat.getIdClase() == mensajeChatDTO.getId_clase()) {
                        claseChat.getMensajes().add(idMex);
                    }
                });
            }
            cursoChat.setUltimo(idMex);
            cursoChatRepo.save(cursoChat);

            // Actualiza el estado del usuario incluida la respuesta
            UsuarioChat usuarioChat = usuarioChatRepo.findByIdUsuario(mensajeChatDTO.getId_usuario());
            if (usuarioChat != null) {
                List<CursoChat> cursoChatList = this.cursoChatRepo.findAllById(usuarioChat.getCursos());
                Boolean presente = false;
                for (CursoChat curso : cursoChatList) {
                    if (curso.getIdCurso().equals(mensajeChatDTO.getId_curso())) {
                        presente = true;
                        break;
                    }
                }
                if (!presente) {
                    cursoChatList.add(cursoChat);
                }
                List<String> IDs = new ArrayList<>();
                cursoChatList.forEach((cur) -> {
                    IDs.add(cur.getId());
                });
                usuarioChat.setCursos(IDs);
                usuarioChatRepo.save(usuarioChat);

                // Controla la respuesta
                if (resp != null) {
                    MensajeChat respuesta = mensajeChatRepo.findById(resp).get();
                    UsuarioChat usuarioRespuesta = usuarioChatRepo.findByIdUsuario(respuesta.getIdUsuario());
                    List<String> mensajes = usuarioRespuesta.getMensajes();
                    mensajes.add(mex.getId());
                    usuarioRespuesta.setMensajes(mensajes);
                    usuarioChatRepo.save(usuarioRespuesta);
                }
            } else {
                System.err.println("No se pudo guardar el mensaje, el usuario no existe");
            }

            // Avisar a los profesores en caso de preguntas
            if (mensajeChatDTO.getPregunta() != null) {
                List<Usuario> profes = cursoRepo.findById(mensajeChat.getIdCurso()).get().getProfesores_curso();
                for (Usuario usuario : profes) {
                    UsuarioChat profeChat = usuarioChatRepo.findByIdUsuario(usuario.getId_usuario());
                    List<String> mensajes = profeChat.getMensajes();
                    mensajes.add(mensajeChat.getId());
                    profeChat.setMensajes(mensajes);
                    usuarioChatRepo.save(profeChat);
                }
            }

        } catch (JsonProcessingException e) {
            // Manejar errores de parseo
            System.err.println("Error al parsear el mensaje JSON: " + e.getMessage());
        }
    }

    public void creaUsuarioChat(String message) {
        // Crear una instancia de ObjectMapper para parsear el JSON
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Convertir el JSON en un objeto MensajeChatDTO
            Usuario usuario = objectMapper.readValue(message, Usuario.class);
            UsuarioChat usuarioChat = new UsuarioChat(
                    null, // String id
                    usuario.getId_usuario(), // Long id_usuario
                    new ArrayList<String>(), // List<CursoChat> cursos
                    new ArrayList<String>()); // List<String> mensajes
            usuarioChatRepo.save(usuarioChat);
        } catch (Exception e) {
            System.err.println("Error en crear el usuario del chat: " + e.getMessage());
        }
    }

    public void creaCursoChat(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Convertir el JSON en un objeto MensajeChatDTO
            Curso curso = objectMapper.readValue(message, Curso.class);

            List<Clase> clases = curso.getClases_curso();
            List<ClaseChat> clasesChat = new ArrayList<>();
            if (clases != null && clases.size() > 0) {
                for (Clase clase : clases) {
                    ClaseChat claseChat = new ClaseChat(
                            clase.getId_clase(), // Long id_clase
                            curso.getId_curso(), // Long id_curso
                            new ArrayList<String>()); // List<String> mensajes
                    clasesChat.add(claseChat);
                }
            }

            CursoChat cursoChat = new CursoChat(
                    null, // String id
                    curso.getId_curso(), // Long id_curso
                    clasesChat, // List<ClaseChat> clases
                    new ArrayList<String>(),
                    null); // List<String> mensajes
            cursoChatRepo.save(cursoChat);
        } catch (Exception e) {
            System.err.println("Error en crear el usuario del chat: " + e.getMessage());
        }
    }

    public void creaClaseChat(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Clase clase = objectMapper.readValue(message, Clase.class);

            ClaseChat claseChat = new ClaseChat(
                    clase.getId_clase(), // Long id_clase
                    clase.getCurso_clase().getId_curso(), // Long id_curso
                    new ArrayList<String>()); // List<String> mensajes

            CursoChat cursoChat = cursoChatRepo.findByIdCurso(clase.getCurso_clase().getId_curso());
            List<ClaseChat> clasesChat = cursoChat.getClases();
            clasesChat.add(claseChat);
            cursoChat.setClases(clasesChat);
            cursoChatRepo.save(cursoChat);
        } catch (Exception e) {
            System.err.println("Error en crear el usuario del chat: " + e.getMessage());
        }
    }

    public void mensajeLeido(String message) {
        String[] lMex = message.replaceAll("\"", "").split(",");
        UsuarioChat usuario = this.usuarioChatRepo.findByIdUsuario(Long.parseLong(lMex[0]));
        if (usuario != null) {
            List<String> mensajes = usuario.getMensajes();
            mensajes.remove(lMex[1]);
            usuario.setMensajes(mensajes);
            this.usuarioChatRepo.save(usuario);
        }
    }
}
