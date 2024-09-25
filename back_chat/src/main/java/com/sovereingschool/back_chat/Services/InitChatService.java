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
    private CursoChatRepository cursoChatRepo;

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
                this.startWatching(idUsuario);
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
                                respuesta.getIdMensaje(),
                                respuesta.getIdCurso(),
                                respuesta.getIdClase(),
                                respuesta.getIdUsuario(),
                                null,
                                null,
                                this.usuarioRepo.findNombreUsuarioForId(respuesta.getIdUsuario()),
                                null,
                                this.usuarioRepo.findFotosUsuarioForId(respuesta.getIdUsuario()).get(0),
                                null,
                                respuesta.getMensaje(),
                                respuesta.getFecha());
                    }

                    MensajeChatDTO mensajeDTO = new MensajeChatDTO(
                            mensaje.getIdMensaje(),
                            mensaje.getIdCurso(),
                            mensaje.getIdClase(),
                            mensaje.getIdUsuario(),
                            cursoRepo.findNombreCursoById(mensaje.getIdCurso()),
                            claseRepo.findNombreClaseById(mensaje.getIdClase()),
                            usuario.getNombre_usuario(),
                            cursoRepo.findImagenCursoById(mensaje.getIdCurso()),
                            usuario.getFoto_usuario().get(0),
                            respuestaDTO,
                            mensaje.getMensaje(),
                            mensaje.getFecha());
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
                                    respuesta.getIdMensaje(),
                                    respuesta.getIdCurso(),
                                    respuesta.getIdClase(),
                                    respuesta.getIdUsuario(),
                                    null,
                                    null,
                                    this.usuarioRepo.findNombreUsuarioForId(respuesta.getIdUsuario()),
                                    null,
                                    this.usuarioRepo.findFotosUsuarioForId(respuesta.getIdUsuario()).get(0),
                                    null,
                                    respuesta.getMensaje(),
                                    respuesta.getFecha());
                        }
                        MensajeChat mex = this.mensajeChatRepo.findById(clase.getMensajes().getFirst()).get();
                        mensaje.add(new MensajeChatDTO(
                                mex.getIdMensaje(),
                                mex.getIdCurso(),
                                mex.getIdClase(),
                                mex.getIdUsuario(),
                                this.cursoRepo.findNombreCursoById(
                                        (this.mensajeChatRepo.findById(clase.getMensajes().getFirst()).get())
                                                .getIdCurso()),
                                this.claseRepo.findNombreClaseById(
                                        (this.mensajeChatRepo.findById(clase.getMensajes().getFirst()).get())
                                                .getIdClase()),
                                this.usuarioRepo.findNombreUsuarioForId(
                                        (this.mensajeChatRepo.findById(clase.getMensajes().getFirst()).get())
                                                .getIdUsuario()),
                                this.cursoRepo.findImagenCursoById(
                                        (this.mensajeChatRepo.findById(clase.getMensajes().getFirst()).get())
                                                .getIdCurso()),
                                this.usuarioRepo.findFotosUsuarioForId(
                                        (this.mensajeChatRepo.findById(clase.getMensajes().getFirst()).get())
                                                .getIdUsuario())
                                        .get(0),
                                respuestaDTO,
                                mex.getMensaje(),
                                mex.getFecha()));

                        clasesDTO.add(new ClaseChatDTO(
                                clase.getIdClase(),
                                clase.getIdCurso(),
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

    public void startWatching(Long idUsuario) {
        executorService.submit(() -> {
            try {
                List<Bson> pipeline = Arrays.asList(
                        Aggregates.match(Filters.eq("idUsuario", idUsuario)));

                collection.watch(pipeline).forEach((ChangeStreamDocument<Document> change) -> {
                    System.out.println("Cambio detectado: " + change.getFullDocument());
                    notifyFrontend(change.getFullDocument());
                });
            } catch (Exception e) {
                System.err.println("Error al observar cambios: " + e.getMessage());
            }
        });
    }

    private void notifyFrontend(Document document) {
        try {
            Long idUsuario = document.getLong("idUsuario");
            List<MensajeChatDTO> mensajesDTO = new ArrayList<>();
            List<CursoChatDTO> cursosDTO = new ArrayList<>();

            if (document.containsKey("mensajes")) {
                List<MensajeChat> mensajes = document.getList("mensajes", MensajeChat.class);
                if (mensajes != null && mensajes.size() > 0) {
                    for (MensajeChat mensaje : mensajes) {
                        MensajeChatDTO mensajeDTO = new MensajeChatDTO();
                        mensajeDTO.setId_mensaje(mensaje.getIdMensaje());
                        mensajeDTO.setId_curso(mensaje.getIdCurso());
                        mensajeDTO.setId_clase(mensaje.getIdClase());
                        mensajeDTO.setId_usuario(mensaje.getIdUsuario());
                        mensajeDTO.setNombre_curso(cursoRepo.findNombreCursoById(mensaje.getIdCurso()));
                        mensajeDTO.setNombre_clase(claseRepo.findNombreClaseById(mensaje.getIdClase()));
                        mensajeDTO.setNombre_usuario(usuarioRepo.findNombreUsuarioForId(mensaje.getIdUsuario()));
                        mensajeDTO.setFoto_curso(cursoRepo.findImagenCursoById(mensaje.getIdCurso()));
                        mensajeDTO.setFoto_usuario(usuarioRepo.findNombreUsuarioForId(mensaje.getIdUsuario()));
                        mensajeDTO.setMensaje(mensaje.getMensaje());
                        mensajesDTO.add(mensajeDTO);
                    }
                }
            }

            if (document.containsKey("cursos")) {
                List<CursoChat> cursos = document.getList("cursos", CursoChat.class);
                if (cursos != null && cursos.size() > 0) {
                    for (CursoChat curso : cursos) {
                        CursoChatDTO cursoDTO = new CursoChatDTO();
                        cursoDTO.setId_curso(curso.getIdCurso());
                        cursoDTO.setNombre_curso(cursoRepo.findNombreCursoById(curso.getIdCurso()));
                        cursoDTO.setFoto_curso(cursoRepo.findImagenCursoById(curso.getIdCurso()));
                        cursosDTO.add(cursoDTO);
                    }
                }
            }

            InitChatDTO updateDTO = new InitChatDTO(idUsuario, mensajesDTO, cursosDTO);
            simpMessagingTemplate.convertAndSend("/init_chat/result", updateDTO);
        } catch (Exception e) {
            System.err.println("Error al notificar al frontend: " + e.getMessage());
        }
    }
}
