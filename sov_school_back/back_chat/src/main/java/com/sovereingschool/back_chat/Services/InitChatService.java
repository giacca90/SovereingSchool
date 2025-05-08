package com.sovereingschool.back_chat.Services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.sovereingschool.back_chat.DTOs.ClaseChatDTO;
import com.sovereingschool.back_chat.DTOs.CursoChatDTO;
import com.sovereingschool.back_chat.DTOs.InitChatDTO;
import com.sovereingschool.back_chat.DTOs.MensajeChatDTO;
import com.sovereingschool.back_chat.Models.ClaseChat;
import com.sovereingschool.back_chat.Models.CursoChat;
import com.sovereingschool.back_chat.Models.MensajeChat;
import com.sovereingschool.back_chat.Models.UsuarioChat;
import com.sovereingschool.back_chat.Repositories.CursoChatRepository;
import com.sovereingschool.back_chat.Repositories.MensajeChatRepository;
import com.sovereingschool.back_chat.Repositories.UsuarioChatRepository;
import com.sovereingschool.back_common.Repositories.ClaseRepository;
import com.sovereingschool.back_common.Repositories.CursoRepository;
import com.sovereingschool.back_common.Repositories.UsuarioRepository;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import reactor.core.publisher.Flux;

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
    private CursoChatRepository cursoChatRepo;

    @Autowired
    private ClaseRepository claseRepo;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    public InitChatDTO initChat(Long idUsuario) {
        // Validar que el ID no sea nulo
        if (idUsuario == null) {
            throw new IllegalArgumentException("El ID de usuario no puede ser nulo");
        }

        UsuarioChat usuarioChat = this.usuarioChatRepo.findByIdUsuario(idUsuario);
        if (usuarioChat == null) {
            return new InitChatDTO();
        }

        List<MensajeChatDTO> mensajesDTO = new ArrayList<>();
        if (usuarioChat.getMensajes() != null && !usuarioChat.getMensajes().isEmpty()) {
            List<MensajeChat> mensajes = this.mensajeChatRepo.findAllById(usuarioChat.getMensajes());
            if (mensajes != null && !mensajes.isEmpty()) {
                mensajesDTO = getMensajesDTO(mensajes);
            }
        }

        List<CursoChatDTO> cursosDTO = new ArrayList<>();
        if (usuarioChat.getCursos() != null && !usuarioChat.getCursos().isEmpty()) {
            List<CursoChat> cursos = this.cursoChatRepo.findAllById(usuarioChat.getCursos());
            if (cursos != null && !cursos.isEmpty()) {
                for (CursoChat curso : cursos) {
                    if (curso.getUltimo() != null) {
                        CursoChatDTO cursoDTO = new CursoChatDTO();
                        cursoDTO.setId_curso(curso.getIdCurso());
                        cursoDTO.setNombre_curso(cursoRepo.findNombreCursoById(curso.getIdCurso()).orElseThrow(() -> {
                            System.err.println("Error en obtener el nombre del curso");
                            return new EntityNotFoundException("Error en obtener el nombre del curso");
                        }));
                        cursoDTO.setFoto_curso(cursoRepo.findImagenCursoById(curso.getIdCurso()).orElseThrow(() -> {
                            System.err.println("Error en obtener la imagen del curso");
                            return new EntityNotFoundException("Error en obtener la imagen del curso");
                        }));

                        Optional<MensajeChat> ultimoMensaje = this.mensajeChatRepo.findById(curso.getUltimo());
                        if (ultimoMensaje.isPresent()) {
                            List<MensajeChatDTO> ultimoDTO = this.getMensajesDTO(Arrays.asList(ultimoMensaje.get()));
                            cursoDTO.setMensajes(ultimoDTO);
                        }
                        cursosDTO.add(cursoDTO);
                    }
                }
            }
        }
        InitChatDTO initChatDTO = new InitChatDTO(usuarioChat.getIdUsuario(), mensajesDTO, cursosDTO);
        return initChatDTO;
    }

    @PostConstruct
    public void observeMultipleCollections() {
        // Configura las opciones de ChangeStream
        ChangeStreamOptions options = ChangeStreamOptions.builder()
                .build();

        Flux<Document> userChatFlux = reactiveMongoTemplate
                .changeStream("users_chat", options, Document.class)
                .map(event -> {
                    Document doc = event.getBody();
                    if (doc == null) {
                        System.err.println("Documento nulo detectado en users_chat stream");
                        return new Document(); // Devolvemos un documento vacío en lugar de null
                    }
                    return doc;
                });

        // Configura el ChangeStream y escucha los eventos
        userChatFlux.subscribe(
                changedDocument -> {
                    try {
                        notifyUsersChat(changedDocument);
                    } catch (Exception e) {
                        System.err.println("Error procesando documento de users_chat: " + e.getMessage());
                    }
                },
                error -> System.err.println("Error en el stream de users_chat: " + error.getMessage()));

        Flux<Document> coursesChatFlux = reactiveMongoTemplate
                .changeStream("courses_chat", options, Document.class)
                .map(event -> {
                    Document doc = event.getBody();
                    if (doc == null) {
                        System.err.println("Documento nulo detectado en courses_chat stream");
                        return new Document(); // Devolvemos un documento vacío en lugar de null
                    }
                    return doc;
                });

        coursesChatFlux.subscribe(
                changedDocument -> {
                    try {
                        notifyCoursesChat(changedDocument);
                    } catch (Exception e) {
                        System.err.println("Error procesando documento de courses_chat: " + e.getMessage());
                    }
                },
                error -> System.err.println("Error en el stream de courses_chat: " + error.getMessage()));
    }

    /**
     * Convierte una lista de Mensajes en una lista de MensajesDTO
     * Gestiona tambien la respuesta de los mensajes y las preguntas
     * 
     * @param mensajes Lista de mensajes
     * @return Lista de MensajesDTO
     */
    List<MensajeChatDTO> getMensajesDTO(List<MensajeChat> mensajes) {
        List<MensajeChatDTO> mensajesDTO = new ArrayList<>();
        for (MensajeChat mensaje : mensajes) {
            MensajeChat respuesta = null;
            if (mensaje.getRespuesta() != null) {
                respuesta = mensajeChatRepo.findById(mensaje.getRespuesta()).get();
            }
            MensajeChatDTO respuestaDTO = null;
            if (respuesta != null) {
                String nombreUsuario = this.usuarioRepo.findNombreUsuarioForId(respuesta.getIdUsuario())
                        .orElseThrow(() -> {
                            System.err.println("Error en obtener el nombre del usuario");
                            return new EntityNotFoundException("Error en obtener el nombre del usuario");
                        });
                respuestaDTO = new MensajeChatDTO(
                        respuesta.getId(), // String id_mensaje
                        respuesta.getIdCurso(), // Long id_curso
                        respuesta.getIdClase(), // Long id_clase
                        respuesta.getIdUsuario(), // Long id_usuario
                        null, // String nombre_curso
                        null, // String nombre_clase
                        nombreUsuario, // String
                        // nombre_usuario
                        null, // String foto_curso
                        this.usuarioRepo.findFotosUsuarioForId(respuesta.getIdUsuario()).get(0).split(",")[0], // String
                        // foto_usuario
                        null, // MensajeChatDTO respuesta
                        respuesta.getMomento(), // int momento
                        respuesta.getMensaje(), // String mensaje
                        respuesta.getFecha()); // Date fecha
            }

            String nombreUsuario = this.usuarioRepo.findNombreUsuarioForId(mensaje.getIdUsuario())
                    .orElseThrow(() -> {
                        System.err.println("Error en obtener el nombre del usuario");
                        return new EntityNotFoundException("Error en obtener el nombre del usuario");
                    });

            String nombreCurso = cursoRepo.findNombreCursoById(mensaje.getIdCurso()).orElseThrow(() -> {
                System.err.println("Error en obtener el nombre del curso en getMensajesDTO");
                return new EntityNotFoundException("Error en obtener el nombre del curso");
            });

            String nombreClase = claseRepo.findNombreClaseById(mensaje.getIdClase()).orElseThrow(() -> {
                System.err.println("Error en obtener el nombre de la clase en getMensajesDTO");
                return new EntityNotFoundException("Error en obtener el nombre de la clase");
            });

            String imagenCurso = cursoRepo.findImagenCursoById(mensaje.getIdCurso()).orElseThrow(() -> {
                System.err.println("Error en obtener la imagen del curso en getMensajesDTO");
                return new EntityNotFoundException("Error en obtener la imagen del curso");
            });

            MensajeChatDTO mensajeDTO = new MensajeChatDTO(
                    mensaje.getId(), // String id_mensaje
                    mensaje.getIdCurso(), // Long id_curso
                    mensaje.getIdClase(), // Long id_clase
                    mensaje.getIdUsuario(), // Long id_usuario
                    nombreCurso, // String nombre_curso
                    nombreClase, // String nombre_clase
                    nombreUsuario, // String nombre_usuario
                    imagenCurso, // String foto_curso
                    usuarioRepo.findFotosUsuarioForId(mensaje.getIdUsuario()).get(0).split(",")[0], // String
                                                                                                    // foto_usuario
                    respuestaDTO, // MensajeChatDTO respuesta
                    mensaje.getMomento(), // int momento
                    mensaje.getMensaje(), // String mensaje
                    mensaje.getFecha()); // Date fecha
            mensajesDTO.add(mensajeDTO);
        }
        return mensajesDTO;
    }

    /**
     * Función que notifica al usuario de los cambios en el chat de un curso
     * 
     * @param document Documento que contiene los cambios
     *                 El documento tiene que ser de tipo CursoChat
     */
    private void notifyCoursesChat(Document document) {
        Long id_curso = document.getLong("idCurso");
        List<ClaseChatDTO> clases = new ArrayList<>();
        List<MensajeChatDTO> mensajes = new ArrayList<>();
        String nombre_curso = this.cursoRepo.findNombreCursoById(id_curso).orElseThrow(() -> {
            System.err.println("Error en obtener el nombre del curso");
            return new EntityNotFoundException("Error en obtener el nombre del curso");
        });
        String foto_curso = this.cursoRepo.findImagenCursoById(id_curso).orElseThrow(() -> {
            System.err.println("Error en obtener la imagen del curso");
            return new EntityNotFoundException("Error en obtener la imagen del curso");
        });

        List<String> mensajesIDs = document.getList("mensajes", String.class);
        if (mensajesIDs != null && mensajesIDs.size() > 0) {
            List<MensajeChat> mensajesChat = this.mensajeChatRepo.findAllById(mensajesIDs);
            if (mensajesChat != null && mensajesChat.size() > 0) {
                mensajes = getMensajesDTO(mensajesChat);
            }
        }

        List<Document> clasesChatD = document.getList("clases", Document.class);
        if (clasesChatD != null && clasesChatD.size() > 0) {
            for (Document claseChatD : clasesChatD) {
                List<String> mensajesClase = claseChatD.getList("mensajes", String.class);
                List<MensajeChatDTO> mensajesClaseDTO = new ArrayList<>();
                if (mensajesClase != null && mensajesClase.size() > 0) {
                    List<MensajeChat> mensajesChatClase = this.mensajeChatRepo.findAllById(mensajesClase);
                    if (mensajesChatClase != null && mensajesChatClase.size() > 0) {
                        mensajesClaseDTO = getMensajesDTO(mensajesChatClase);
                    }
                }
                String nombreClase = this.claseRepo.findNombreClaseById(claseChatD.getLong("idClase"))
                        .orElseThrow(() -> {
                            System.err.println("Error en obtener el nombre de la clase en notifyCourseChat");
                            return new EntityNotFoundException("Error en obtener el nombre de la clase");
                        });
                clases.add(new ClaseChatDTO(claseChatD.getLong("idClase"), claseChatD.getLong("idCurso"),
                        nombreClase, mensajesClaseDTO));
            }
        }

        CursoChatDTO cursoChatDTO = new CursoChatDTO(id_curso, clases, mensajes, nombre_curso, foto_curso);
        simpMessagingTemplate.convertAndSend("/init_chat/" + id_curso, cursoChatDTO);
    }

    /**
     * Notifica al usuario de cambios en sus chats
     * 
     * @param document Documento de chat del usuario
     */
    private void notifyUsersChat(Document document) {
        // ("DOCUMENTO MODIFICADO: " + document);
        Long idUsuario = document.getLong("idUsuario");
        List<MensajeChatDTO> mensajesDTO = new ArrayList<>();
        List<CursoChatDTO> cursosDTO = new ArrayList<>();

        List<String> mensajesS = document.getList("mensajes", String.class);
        if (mensajesS != null && mensajesS.size() > 0) {
            List<MensajeChat> mensajes = new ArrayList<>();
            mensajesS.forEach((mexs) -> {
                mensajes.add(this.mensajeChatRepo.findById(mexs).get());
            });
            mensajesDTO = getMensajesDTO(mensajes);
        }

        List<String> cursosS = document.getList("cursos", String.class);
        if (cursosS != null && cursosS.size() > 0) {
            for (String cursoS : cursosS) {
                CursoChat cursoD = cursoChatRepo.findById(cursoS).get();
                Long idCurso = cursoD.getIdCurso();
                List<ClaseChatDTO> clasesDTO = new ArrayList<>();
                List<ClaseChat> clasesD = cursoD.getClases();
                if (clasesD != null && clasesD.size() > 0) {
                    for (ClaseChat claseD : clasesD) {
                        List<MensajeChatDTO> mensajesChatDTO = new ArrayList<>();
                        List<String> mex = claseD.getMensajes();
                        List<MensajeChat> mensajesChat = this.mensajeChatRepo.findAllById(mex);
                        if (mensajesChat != null && mensajesChat.size() > 0) {
                            mensajesChatDTO = getMensajesDTO(mensajesChat);
                        }
                        String nombreClase = this.claseRepo.findNombreClaseById(claseD.getIdClase())
                                .orElseThrow(() -> {
                                    System.err.println("Error en obtener el nombre de la clase");
                                    return new EntityNotFoundException("Error en obtener el nombre de la clase");
                                });
                        clasesDTO.add(new ClaseChatDTO(
                                claseD.getIdClase(), // id_clase;
                                claseD.getIdCurso(), // id_curso;
                                nombreClase, // nombre_clase;
                                mensajesChatDTO// mensajes;
                        ));
                    }
                }
                String nombreCurso = this.cursoRepo.findNombreCursoById(idCurso).orElseThrow(() -> {
                    System.err.println("Error en obtener el nombre del curso");
                    return new EntityNotFoundException("Error en obtener el nombre del curso");
                });
                String fotoCurso = this.cursoRepo.findImagenCursoById(idCurso).orElseThrow(() -> {
                    System.err.println("Error en obtener la imagen del curso");
                    return new EntityNotFoundException("Error en obtener la imagen del curso");
                });
                cursosDTO.add(new CursoChatDTO(
                        idCurso,
                        clasesDTO,
                        mensajesDTO,
                        nombreCurso, // nombre_curso;
                        fotoCurso // foto_curso;
                ));
            }
        }

        InitChatDTO updateDTO = new InitChatDTO(idUsuario, mensajesDTO, cursosDTO);
        simpMessagingTemplate.convertAndSend("/init_chat/result", updateDTO);
    }
}