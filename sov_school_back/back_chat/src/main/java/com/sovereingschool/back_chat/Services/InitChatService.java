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
import com.sovereingschool.back_common.Models.Curso;
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

    /**
     * Función para inicializar el chat de un usuario
     *
     * @param idUsuario el id del usuario
     * @return InitChatDTO con los los chats del usuario
     * @throws IllegalArgumentException si el idUsuario es nulo
     * @throws EntityNotFoundException  si el usuario no existe
     */
    public InitChatDTO initChat(Long idUsuario) {
        // Validar que el ID no sea nulo
        if (idUsuario == null) {
            throw new IllegalArgumentException("El ID de usuario no puede ser nulo");
        }
        UsuarioChat usuarioChat = this.usuarioChatRepo.findByIdUsuario(idUsuario).orElseThrow(() -> {
            System.err.println("Error en obtener el usuario chat: no se encuentra el documento");
            throw new EntityNotFoundException("Error en obtener el usuario chat: no se encuentra el documento");
        });

        // Obtiene los mensajes del usuario
        List<MensajeChatDTO> mensajesDTO = new ArrayList<>();
        if (usuarioChat.getMensajes() != null && !usuarioChat.getMensajes().isEmpty()) {
            List<MensajeChat> mensajes = this.mensajeChatRepo.findAllById(usuarioChat.getMensajes());
            if (mensajes != null && !mensajes.isEmpty()) {
                mensajesDTO = getMensajesDTO(mensajes);
            }
        }

        // Obtiene los cursos del usuario
        List<CursoChatDTO> cursosDTO = new ArrayList<>();
        if (usuarioChat.getCursos() != null && !usuarioChat.getCursos().isEmpty()) {
            List<CursoChat> cursosChat = this.cursoChatRepo.findAllById(usuarioChat.getCursos());
            if (cursosChat != null && !cursosChat.isEmpty()) {
                for (CursoChat cursoChat : cursosChat) {
                    Curso curso = cursoRepo.findById(cursoChat.getIdCurso()).orElseThrow(() -> {
                        System.err.println("Error en obtener el curso con ID " + cursoChat.getIdCurso());
                        throw new EntityNotFoundException("Error en obtener el curso con ID " + cursoChat.getIdCurso());
                    });
                    if (cursoChat.getUltimo() != null) {
                        CursoChatDTO cursoDTO = new CursoChatDTO();
                        cursoDTO.setId_curso(cursoChat.getIdCurso());
                        cursoDTO.setNombre_curso(curso.getNombre_curso());
                        cursoDTO.setFoto_curso(curso.getImagen_curso());

                        Optional<MensajeChat> ultimoMensaje = this.mensajeChatRepo.findById(cursoChat.getUltimo());
                        if (ultimoMensaje.isPresent()) {
                            List<MensajeChatDTO> ultimoDTO = this.getMensajesDTO(Arrays.asList(ultimoMensaje.get()));
                            cursoDTO.setMensajes(ultimoDTO);
                        } else {
                            MensajeChatDTO noMessage = new MensajeChatDTO();
                            noMessage.setId_curso(cursoChat.getIdCurso());
                            noMessage.setNombre_curso(curso.getNombre_curso());
                            noMessage.setFoto_curso(curso.getImagen_curso());
                            noMessage.setMensaje("No hay mensajes en este curso");

                            cursoDTO.setMensajes(Arrays.asList(noMessage));
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
                respuesta = mensajeChatRepo.findById(mensaje.getRespuesta()).orElseThrow(() -> {
                    System.err.println("Error en obtener la respuesta del mensaje");
                    throw new EntityNotFoundException("Error en obtener la respuesta del mensaje");
                });
            }
            MensajeChatDTO respuestaDTO = null;
            if (respuesta != null) {
                String nombreUsuario = this.usuarioRepo.findNombreUsuarioForId(respuesta.getIdUsuario())
                        .orElseThrow(() -> {
                            System.err.println("Error en obtener el nombre del usuario");
                            throw new EntityNotFoundException("Error en obtener el nombre del usuario");
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
                        throw new EntityNotFoundException("Error en obtener el nombre del usuario");
                    });

            Curso curso = cursoRepo.findById(mensaje.getIdCurso()).orElseThrow(() -> {
                System.err.println("Error en obtener el curso con ID " + mensaje.getIdCurso());
                throw new EntityNotFoundException("Error en obtener el curso con ID " + mensaje.getIdCurso());
            });

            String nombreCurso = curso.getNombre_curso();
            String imagenCurso = curso.getImagen_curso();
            String nombreClase = curso.getClases_curso().stream()
                    .filter(clase -> clase.getId_clase().equals(mensaje.getIdClase()))
                    .map(clase -> clase.getNombre_clase())
                    .findFirst()
                    .orElseThrow(() -> {
                        System.err.println("Error en obtener el nombre de la clase con ID " + mensaje.getIdClase());
                        throw new EntityNotFoundException(
                                "Error en obtener el nombre de la clase con ID " + mensaje.getIdClase());
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
        Curso curso = cursoRepo.findById(id_curso).orElseThrow(() -> {
            System.err.println("Error en obtener el curso con ID " + id_curso);
            throw new EntityNotFoundException("Error en obtener el curso con ID " + id_curso);
        });
        String nombre_curso = curso.getNombre_curso();
        String foto_curso = curso.getImagen_curso();

        List<String> mensajesIDs = document.getList("mensajes", String.class);
        if (mensajesIDs != null && !mensajesIDs.isEmpty()) {
            List<MensajeChat> mensajesChat = this.mensajeChatRepo.findAllById(mensajesIDs);
            if (mensajesChat != null && !mensajesChat.isEmpty()) {
                mensajes = getMensajesDTO(mensajesChat);
            }
        }

        List<Document> clasesChatDocument = document.getList("clases", Document.class);
        if (clasesChatDocument != null && clasesChatDocument.size() > 0) {
            for (Document claseChatDocument : clasesChatDocument) {
                List<String> mensajesClase = claseChatDocument.getList("mensajes", String.class);
                List<MensajeChatDTO> mensajesClaseDTO = new ArrayList<>();
                if (mensajesClase != null && mensajesClase.size() > 0) {
                    List<MensajeChat> mensajesChatClase = this.mensajeChatRepo.findAllById(mensajesClase);
                    if (mensajesChatClase != null && mensajesChatClase.size() > 0) {
                        mensajesClaseDTO = getMensajesDTO(mensajesChatClase);
                    }
                }
                String nombreClase = this.claseRepo.findNombreClaseById(claseChatDocument.getLong("idClase"))
                        .orElseThrow(() -> {
                            System.err.println("Error en obtener el nombre de la clase en notifyCourseChat");
                            throw new EntityNotFoundException("Error en obtener el nombre de la clase");
                        });
                clases.add(new ClaseChatDTO(claseChatDocument.getLong("idClase"), claseChatDocument.getLong("idCurso"),
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
                                    throw new EntityNotFoundException("Error en obtener el nombre de la clase");
                                });
                        clasesDTO.add(new ClaseChatDTO(
                                claseD.getIdClase(), // id_clase;
                                claseD.getIdCurso(), // id_curso;
                                nombreClase, // nombre_clase;
                                mensajesChatDTO// mensajes;
                        ));
                    }
                }
                Curso curso = cursoRepo.findById(idCurso).orElseThrow(() -> {
                    System.err.println("Error en obtener el curso con ID " + idCurso);
                    throw new EntityNotFoundException("Error en obtener el curso con ID " + idCurso);
                });
                String nombreCurso = curso.getNombre_curso();
                String fotoCurso = curso.getImagen_curso();
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