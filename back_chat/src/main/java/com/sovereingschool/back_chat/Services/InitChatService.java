package com.sovereingschool.back_chat.Services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.sovereingschool.back_chat.DTOs.ClaseChatDTO;
import com.sovereingschool.back_chat.DTOs.CursoChatDTO;
import com.sovereingschool.back_chat.DTOs.InitChatDTO;
import com.sovereingschool.back_chat.DTOs.MensajeChatDTO;
import com.sovereingschool.back_chat.Models.ClaseChat;
import com.sovereingschool.back_chat.Models.CursoChat;
import com.sovereingschool.back_chat.Models.MensajeChat;
import com.sovereingschool.back_chat.Models.Usuario;
import com.sovereingschool.back_chat.Models.UsuarioChat;
import com.sovereingschool.back_chat.Repositories.ClaseRepository;
import com.sovereingschool.back_chat.Repositories.CursoRepository;
import com.sovereingschool.back_chat.Repositories.MensajeChatRepository;
import com.sovereingschool.back_chat.Repositories.UsuarioChatRepository;
import com.sovereingschool.back_chat.Repositories.UsuarioRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class InitChatService {
    @Autowired
    private UsuarioChatRepository usuarioChatRepo;

    @Autowired
    private MensajeChatRepository mensajeChatRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private CursoRepository cursoRepo;

    @Autowired
    private ClaseRepository claseRepo;

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private MongoCollection<Document> collection;
    private final ExecutorService executorService;

    public InitChatService() {
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @PostConstruct
    public void init() {
        this.collection = mongoClient.getDatabase("SovSchoolChat").getCollection("users_chat");
    }

    public InitChatDTO initChat(Long idUsuario) {
        try {
            UsuarioChat usuarioChat = this.usuarioChatRepo.findByIdUsuario(idUsuario);
            System.out.println("USUARIOCHAT: " + usuarioChat);
            if (usuarioChat == null) {
                usuarioChat = new UsuarioChat(null, 0L, null, null); // Objeto por defecto si no se encuentra
                return new InitChatDTO();
            } else {
                this.startWatchingUsuario(idUsuario);
            }
            List<MensajeChat> mensajes = usuarioChat.getMensajes();
            List<CursoChat> cursos = usuarioChat.getCursos();

            List<MensajeChatDTO> mensajesDTO = new ArrayList<>();
            if (mensajes != null && mensajes.size() > 0) {
                for (MensajeChat mensaje : mensajes) {
                    Usuario usuario = usuarioRepo.findById(mensaje.getIdUsuario()).get();
                    String respuestaId = mensaje.getRespuesta();
                    MensajeChatDTO respuestaDTO = null;
                    if (respuestaId != null) {
                        MensajeChat respuesta = this.mensajeChatRepo.findById(respuestaId).get();
                        respuestaDTO = new MensajeChatDTO(
                                respuesta.getId(), // String id_mensaje
                                respuesta.getIdCurso(), // Long id_curso
                                respuesta.getIdClase(), // Long id_clase
                                respuesta.getIdUsuario(), // Long id_usuario
                                null, // String nombre_curso
                                null, // String nombre_clase
                                this.usuarioRepo.findNombreUsuarioForId(respuesta.getIdUsuario()), // String
                                                                                                   // nombre_usuario
                                null, // String foto_curso
                                this.usuarioRepo.findFotosUsuarioForId(respuesta.getIdUsuario()).get(0), // String
                                                                                                         // foto_usuario
                                null, // MensajeChatDTO respuesta
                                respuesta.getMensaje(), // String mensaje
                                respuesta.getFecha()); // Date fecha
                    }

                    MensajeChatDTO mensajeDTO = new MensajeChatDTO(
                            mensaje.getId(), // String id_mensaje
                            mensaje.getIdCurso(), // Long id_curso
                            mensaje.getIdClase(), // Long id_clase
                            mensaje.getIdUsuario(), // Long id_usuario
                            cursoRepo.findNombreCursoById(mensaje.getIdCurso()), // String nombre_curso
                            claseRepo.findNombreClaseById(mensaje.getIdClase()), // String nombre_clase
                            usuario.getNombre_usuario(), // String nombre_usuario
                            cursoRepo.findImagenCursoById(mensaje.getIdCurso()), // String foto_curso
                            usuario.getFoto_usuario().get(0), // String foto_usuario
                            respuestaDTO, // MensajeChatDTO respuesta
                            mensaje.getMensaje(), // String mensaje
                            mensaje.getFecha()); // Date fecha
                    mensajesDTO.add(mensajeDTO);
                }
            }

            List<CursoChatDTO> cursosDTO = new ArrayList<>();
            if (cursos != null && cursos.size() > 0) {
                for (CursoChat curso : cursos) {
                    CursoChatDTO cursoDTO = new CursoChatDTO();
                    cursoDTO.setId_curso(curso.getIdCurso());
                    cursoDTO.setNombre_curso(cursoRepo.findNombreCursoById(curso.getIdCurso()));
                    cursoDTO.setFoto_curso(cursoRepo.findImagenCursoById(curso.getIdCurso()));
                    List<ClaseChatDTO> clasesDTO = new ArrayList<>();
                    curso.getClases().forEach(clase -> {
                        List<MensajeChatDTO> mensaje = new ArrayList<>();
                        String respuestaId = this.mensajeChatRepo.findById(clase.getMensajes().getFirst()).get()
                                .getRespuesta();
                        MensajeChatDTO respuestaDTO = null;
                        if (respuestaId != null) {
                            MensajeChat respuesta = this.mensajeChatRepo.findById(respuestaId).get();
                            respuestaDTO = new MensajeChatDTO(
                                    respuesta.getId(), // String id_mensaje
                                    respuesta.getIdCurso(), // Long id_curso
                                    respuesta.getIdClase(), // Long id_clase
                                    respuesta.getIdUsuario(), // Long id_usuario
                                    null, // String nombre_curso
                                    null, // String nombre_clase
                                    this.usuarioRepo.findNombreUsuarioForId(respuesta.getIdUsuario()), // String
                                                                                                       // nombre_usuario
                                    null, // String foto_curso
                                    this.usuarioRepo.findFotosUsuarioForId(respuesta.getIdUsuario()).get(0), // String
                                                                                                             // foto_usuario
                                    null, // MensajeChatDTO respuesta
                                    respuesta.getMensaje(), // String mensaje
                                    respuesta.getFecha()); // Date fecha
                        }
                        MensajeChat mex = this.mensajeChatRepo.findById(clase.getMensajes().getFirst()).get();
                        mensaje.add(new MensajeChatDTO(
                                mex.getId(), // String id_mensaje
                                mex.getIdCurso(), // Long id_curso
                                mex.getIdClase(), // Long id_clase
                                mex.getIdUsuario(), // Long id_usuario
                                this.cursoRepo.findNombreCursoById(
                                        (this.mensajeChatRepo.findById(clase.getMensajes().getFirst()).get())
                                                .getIdCurso()), // String nombre_curso
                                this.claseRepo.findNombreClaseById(
                                        (this.mensajeChatRepo.findById(clase.getMensajes().getFirst()).get())
                                                .getIdClase()), // String nombre_clase
                                this.usuarioRepo.findNombreUsuarioForId(
                                        (this.mensajeChatRepo.findById(clase.getMensajes().getFirst()).get())
                                                .getIdUsuario()), // String nombre_usuario
                                this.cursoRepo.findImagenCursoById(
                                        (this.mensajeChatRepo.findById(clase.getMensajes().getFirst()).get())
                                                .getIdCurso()), // String foto_curso
                                this.usuarioRepo.findFotosUsuarioForId(
                                        (this.mensajeChatRepo.findById(clase.getMensajes().getFirst()).get())
                                                .getIdUsuario())
                                        .get(0), // String foto_usuario
                                respuestaDTO, // MensajeChatDTO respuesta
                                mex.getMensaje(), // String mensaje
                                mex.getFecha())); // Date fecha

                        clasesDTO.add(new ClaseChatDTO(
                                clase.getIdClase(), // Long id_clase
                                clase.getIdCurso(), // Long id_curso
                                this.claseRepo.findById(clase.getIdClase()).get().getNombre_clase(), mensaje));
                        cursosDTO.add(cursoDTO);
                    });

                }
            }

            return new InitChatDTO(usuarioChat.getIdUsuario(), mensajesDTO, cursosDTO);
        } catch (Exception e) {
            // Manejo de excepciones
            System.err.println("Error al buscar usuario: " + e.getMessage());
            return new InitChatDTO();
        }

    }

    public void startWatchingUsuario(Long idUsuario) {
        executorService.submit(() -> {
            try {
                List<Bson> pipeline = Arrays.asList(
                        Aggregates.match(Filters.eq("idUsuario", idUsuario)));

                collection.watch(pipeline).forEach((ChangeStreamDocument<Document> change) -> {
                    System.out.println("Cambio detectado: " + change.getFullDocument());
                    notifyFrontendUsuario(change.getFullDocument());
                });
            } catch (Exception e) {
                System.err.println("Error al observar cambios: " + e.getMessage());
            }
        });
    }

    private void notifyFrontendUsuario(Document document) {
        Long idUsuario = document.getLong("idUsuario");
        List<MensajeChatDTO> mensajesDTO = new ArrayList<>();
        List<CursoChatDTO> cursosDTO = new ArrayList<>();

        if (document.containsKey("mensajes")) {
            List<MensajeChat> mensajes = document.getList("mensajes", MensajeChat.class);
            if (mensajes != null && mensajes.size() > 0) {
                for (MensajeChat mensaje : mensajes) {
                    MensajeChat respuesta = mensajeChatRepo.findById(mensaje.getRespuesta()).get();
                    MensajeChatDTO respuestaDTO = null;
                    if (respuesta != null) {
                        respuestaDTO = new MensajeChatDTO(
                                respuesta.getId(), // String id_mensaje
                                respuesta.getIdCurso(), // Long id_curso
                                respuesta.getIdClase(), // Long id_clase
                                respuesta.getIdUsuario(), // Long id_usuario
                                null, // String nombre_curso
                                null, // String nombre_clase
                                this.usuarioRepo.findNombreUsuarioForId(respuesta.getIdUsuario()), // String
                                                                                                   // nombre_usuario
                                null, // String foto_curso
                                this.usuarioRepo.findFotosUsuarioForId(respuesta.getIdUsuario()).get(0), // String
                                                                                                         // foto_usuario
                                null, // MensajeChatDTO respuesta
                                respuesta.getMensaje(), // String mensaje
                                respuesta.getFecha()); // Date fecha
                    }

                    MensajeChatDTO mensajeDTO = new MensajeChatDTO(
                            mensaje.getId(), // String id_mensaje
                            mensaje.getIdCurso(), // Long id_curso
                            mensaje.getIdClase(), // Long id_clase
                            mensaje.getIdUsuario(), // Long id_usuario
                            cursoRepo.findNombreCursoById(mensaje.getIdCurso()), // String nombre_curso
                            claseRepo.findNombreClaseById(mensaje.getIdClase()), // String nombre_clase
                            usuarioRepo.findNombreUsuarioForId(mensaje.getIdUsuario()), // String nombre_usuario
                            cursoRepo.findImagenCursoById(mensaje.getIdCurso()), // String foto_curso
                            usuarioRepo.findNombreUsuarioForId(mensaje.getIdUsuario()), // String foto_usuario
                            respuestaDTO, // MensajeChatDTO respuesta
                            mensaje.getMensaje(), // String mensaje
                            mensaje.getFecha()); // Date fecha
                    mensajesDTO.add(mensajeDTO);
                }
            }
        }

        if (document.containsKey("cursos")) {
            List<CursoChat> cursos = document.getList("cursos", CursoChat.class);
            if (cursos != null && cursos.size() > 0) {
                for (CursoChat curso : cursos) {
                    List<ClaseChat> clases = curso.getClases();
                    List<ClaseChatDTO> clasesDTO = new ArrayList<>();
                    if (clases != null && clases.size() > 0) {
                        for (ClaseChat clase : clases) {
                            List<String> mensajes = clase.getMensajes();
                            List<MensajeChatDTO> mensajesCursoDTO = new ArrayList<>();
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
                                            usuarioRepo.findNombreUsuarioForId(mensaje.getIdUsuario()), // String
                                                                                                        // nombre_usuario
                                            cursoRepo.findImagenCursoById(mensaje.getIdCurso()), // String foto_curso
                                            usuarioRepo.findFotosUsuarioForId(mensaje.getIdUsuario()).get(0), // String
                                            // foto_usuario
                                            null, // MensajeChatDTO respuesta
                                            mensaje.getMensaje(), // String mensaje
                                            mensaje.getFecha() // Date fecha
                                    );
                                    mensajesCursoDTO.add(mensajeDTO);
                                }
                                ClaseChatDTO claseDTO = new ClaseChatDTO(
                                        clase.getIdClase(),
                                        clase.getIdCurso(),
                                        this.claseRepo.findById(clase.getIdClase()).get().getNombre_clase(),
                                        mensajesCursoDTO);
                                clasesDTO.add(claseDTO);
                            }
                        }
                        CursoChatDTO cursoDTO = new CursoChatDTO(
                                curso.getIdCurso(),
                                clasesDTO,
                                mensajesDTO,
                                cursoRepo.findNombreCursoById(curso.getIdCurso()),
                                cursoRepo.findImagenCursoById(curso.getIdCurso()));
                        cursoDTO.setId_curso(curso.getIdCurso());
                        cursoDTO.setNombre_curso(cursoRepo.findNombreCursoById(curso.getIdCurso()));
                        cursoDTO.setFoto_curso(cursoRepo.findImagenCursoById(curso.getIdCurso()));
                        cursosDTO.add(cursoDTO);
                    }
                }
            }
        }

        InitChatDTO updateDTO = new InitChatDTO(idUsuario, mensajesDTO, cursosDTO);
        simpMessagingTemplate.convertAndSend("/init_chat/result", updateDTO);
    }
}
