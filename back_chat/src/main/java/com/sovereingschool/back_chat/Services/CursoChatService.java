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
        System.out.println("LOG1: " + cursoChat.toString());
        CursoChatDTO cursoChatDTO = null;
        if (cursoChat != null) {
            List<ClaseChat> clases = cursoChat.getClases();
            List<ClaseChatDTO> clasesDTO = new ArrayList<>();
            System.out.println("LOG2: " + clases.toString());
            if (clases != null && clases.size() > 0) {
                for (ClaseChat clase : clases) {
                    List<String> mensajes = clase.getMensajes();
                    System.out.println("LOG3: " + mensajes.toString());
                    List<MensajeChatDTO> mensajesDTO = new ArrayList<>();
                    if (mensajes != null && mensajes.size() > 0) {
                        for (String mensajeId : mensajes) {
                            MensajeChat mensaje = mensajeChatRepo.findById(mensajeId).get();
                            MensajeChatDTO mensajeDTO = new MensajeChatDTO(
                                    mensaje.getId(), // String id
                                    mensaje.getIdCurso(), // Long id_curso
                                    mensaje.getIdClase(), // Long id_clase
                                    mensaje.getIdUsuario(), // Long id_usuario
                                    null, // String nombre_curso
                                    null, // String nombre_clase
                                    usuarioRepo.findNombreUsuarioForId(mensaje.getIdUsuario()), // String nombre_usuario
                                    null, // String foto_curso
                                    usuarioRepo.findFotosUsuarioForId(mensaje.getIdUsuario()).get(0), // String
                                                                                                      // foto_usuario
                                    null, // MensajeChatDTO respuesta
                                    mensaje.getMensaje(), // String mensaje
                                    mensaje.getFecha()); // Date fecha
                            mensajesDTO.add(mensajeDTO);
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
            System.out.println("LOG4: " + mensajes.toString());
            if (mensajes != null && mensajes.size() > 0) {
                for (String mensajeId : mensajes) {
                    MensajeChat mensaje = mensajeChatRepo.findById(mensajeId).get();
                    MensajeChatDTO mensajeDTO = new MensajeChatDTO(
                            mensaje.getId(), // String id_mensaje
                            mensaje.getIdCurso(), // Long id_curso
                            mensaje.getIdClase(), // Long id_clase
                            mensaje.getIdUsuario(), // Long id_usuario
                            cursoRepo.findNombreCursoById(mensaje.getIdCurso()), // String nombre_curso
                            claseRepo.findNombreClaseById(mensaje.getIdClase()), // String nombre_clase
                            usuarioRepo.findNombreUsuarioForId(mensaje.getIdUsuario()), // String nombre_usuario
                            null, // String foto_curso
                            usuarioRepo.findFotosUsuarioForId(mensaje.getIdUsuario()).get(0), // String foto_usuario
                            null, // MensajeChatDTO respuesta
                            mensaje.getMensaje(), // String mensaje
                            mensaje.getFecha()); // Date fecha
                    mensajesDTO.add(mensajeDTO);
                }
            }

            cursoChatDTO = new CursoChatDTO(
                    idCurso,
                    clasesDTO,
                    mensajesDTO,
                    cursoRepo.findNombreCursoById(idCurso),
                    cursoRepo.findImagenCursoById(idCurso));
        }

        System.out.println("LOG5: " + cursoChatDTO.toString());
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
            CursoChat cursoChat = new CursoChat(null, curso.getId_curso(), clasesChat, new ArrayList<>());
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

            // Imprimir el objeto parseado (solo para verificación)
            System.out.println("Mensaje recibido: " + mensajeChatDTO);

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
                    mensajeChatDTO.getMensaje(), // String mensaje
                    mensajeChatDTO.getFecha()); // Date fecha
            MensajeChat mex = this.mensajeChatRepo.save(mensajeChat);
            System.out.println("MEX: " + mex);

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
            cursoChatRepo.save(cursoChat);

            // Actualiza el estado del usuario incluida la respuesta
            UsuarioChat usuarioChat = usuarioChatRepo.findByIdUsuario(mensajeChatDTO.getId_usuario());
            if (usuarioChat != null) {
                List<CursoChat> cursoChatList = usuarioChat.getCursos();
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
                usuarioChat.setCursos(cursoChatList);
                usuarioChatRepo.save(usuarioChat);

                // Controla la respuesta
                if (resp != null) {
                    MensajeChat respuesta = mensajeChatRepo.findById(resp).get();
                    UsuarioChat usuarioRespuesta = usuarioChatRepo.findByIdUsuario(respuesta.getIdUsuario());
                    List<MensajeChat> mensajes = usuarioRespuesta.getMensajes();
                    mensajes.add(mex);
                    usuarioRespuesta.setMensajes(mensajes);
                    usuarioChatRepo.save(usuarioRespuesta);
                }
            } else {
                System.err.println("No se pudo guardar el mensaje, el usuario no existe");
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
                    new ArrayList<CursoChat>(), // List<CursoChat> cursos
                    new ArrayList<MensajeChat>()); // List<MensajeChat> mensajes
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
                    new ArrayList<String>()); // List<String> mensajes
            cursoChatRepo.save(cursoChat);
        } catch (Exception e) {
            System.err.println("Error en crear el usuario del chat: " + e.getMessage());
        }
    }
}
