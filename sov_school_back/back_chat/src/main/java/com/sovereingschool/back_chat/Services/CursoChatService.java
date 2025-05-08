package com.sovereingschool.back_chat.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sovereingschool.back_chat.DTOs.ClaseChatDTO;
import com.sovereingschool.back_chat.DTOs.CursoChatDTO;
import com.sovereingschool.back_chat.DTOs.MensajeChatDTO;
import com.sovereingschool.back_chat.Models.ClaseChat;
import com.sovereingschool.back_chat.Models.CursoChat;
import com.sovereingschool.back_chat.Models.MensajeChat;
import com.sovereingschool.back_chat.Models.UsuarioChat;
import com.sovereingschool.back_chat.Repositories.CursoChatRepository;
import com.sovereingschool.back_chat.Repositories.MensajeChatRepository;
import com.sovereingschool.back_chat.Repositories.UsuarioChatRepository;
import com.sovereingschool.back_common.Models.Clase;
import com.sovereingschool.back_common.Models.Curso;
import com.sovereingschool.back_common.Models.Usuario;
import com.sovereingschool.back_common.Repositories.ClaseRepository;
import com.sovereingschool.back_common.Repositories.CursoRepository;
import com.sovereingschool.back_common.Repositories.UsuarioRepository;
import com.sovereingschool.back_common.Utils.JwtUtil;

import jakarta.persistence.EntityNotFoundException;
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

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Función para obtener el chat de un curso
     * 
     * @param idCurso Id del curso
     * @return CursoChatDTO con los datos del curso
     * @throws EntityNotFoundException si el curso no existe
     */
    public CursoChatDTO getChat(Long idCurso) {
        CursoChat cursoChat = cursoChatRepo.findByIdCurso(idCurso);
        CursoChatDTO cursoChatDTO = null;
        if (cursoChat != null) {
            List<ClaseChat> clases = cursoChat.getClases();
            List<ClaseChatDTO> clasesDTO = new ArrayList<>();
            if (clases != null && clases.size() > 0) {
                for (ClaseChat clase : clases) {
                    List<String> mensajes = clase.getMensajes();
                    List<MensajeChatDTO> mensajesDTO = new ArrayList<>();
                    if (mensajes != null && mensajes.size() > 0) {
                        List<MensajeChat> mensajesChat = mensajeChatRepo.findAllById(mensajes);
                        if (mensajesChat != null && mensajesChat.size() > 0) {
                            mensajesDTO = initChatService.getMensajesDTO(mensajesChat);
                        }
                    }
                    String nombreClase = claseRepo.findNombreClaseById(clase.getIdClase()).orElseThrow(() -> {
                        System.err.println("Error en obtener el nombre de la clase en getChat");
                        return new EntityNotFoundException("Error en obtener el nombre de la clase");
                    });
                    clasesDTO.add(new ClaseChatDTO(
                            clase.getIdClase(),
                            cursoChat.getIdCurso(),
                            nombreClase,
                            mensajesDTO));
                }
            }

            List<String> mensajes = cursoChat.getMensajes();
            List<MensajeChatDTO> mensajesDTO = new ArrayList<>();
            if (mensajes != null && mensajes.size() > 0) {
                List<MensajeChat> mensajesChat = mensajeChatRepo.findAllById(mensajes);
                if (mensajesChat != null && mensajesChat.size() > 0) {
                    mensajesDTO = initChatService.getMensajesDTO(mensajesChat);
                }
            }

            String nombreCurso = cursoRepo.findNombreCursoById(idCurso).orElseThrow(() -> {
                System.err.println("Error en obtener el nombre del curso");
                return new EntityNotFoundException("Error en obtener el nombre del curso");
            });

            String imagenCurso = cursoRepo.findImagenCursoById(idCurso).orElseThrow(() -> {
                System.err.println("Error en obtener la imagen del curso");
                return new EntityNotFoundException("Error en obtener la imagen del curso");
            });

            cursoChatDTO = new CursoChatDTO(
                    idCurso,
                    clasesDTO,
                    mensajesDTO,
                    nombreCurso,
                    imagenCurso);
        }
        return cursoChatDTO;
    }

    public void init() {
        try {
            // Inicializar usuarios
            List<Usuario> usuarios = usuarioRepo.findAll();
            for (Usuario usuario : usuarios) {
                if (usuarioChatRepo.findByIdUsuario(usuario.getId_usuario()) != null)
                    continue;
                UsuarioChat usuarioChat = new UsuarioChat(null, usuario.getId_usuario(), new ArrayList<>(),
                        new ArrayList<>());
                usuarioChatRepo.save(usuarioChat);
            }
            // Inicializar cursos
            List<Curso> cursos = cursoRepo.findAll();
            for (Curso curso : cursos) {
                if (cursoChatRepo.findByIdCurso(curso.getId_curso()) != null)
                    continue;
                List<Clase> clases = curso.getClases_curso();
                List<ClaseChat> clasesChat = new ArrayList<>();
                for (Clase clase : clases) {
                    ClaseChat claseChat = new ClaseChat(clase.getId_clase(), curso.getId_curso(), new ArrayList<>());
                    clasesChat.add(claseChat);
                }
                CursoChat cursoChat = new CursoChat(null, curso.getId_curso(), clasesChat, new ArrayList<>(), null);
                cursoChatRepo.save(cursoChat);
                List<Usuario> profesores = curso.getProfesores_curso();
                for (Usuario profesor : profesores) {
                    UsuarioChat profChat = usuarioChatRepo.findByIdUsuario(profesor.getId_usuario());
                    profChat.getCursos().add(cursoChat.getId());
                    usuarioChatRepo.save(profChat);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en inicializar chat: " + e.getMessage());
            throw new RuntimeException("Error en inicializar chat: " + e.getMessage());
        }
    }

    /**
     * Función para guardar un mensaje en el chat
     *
     * @param message Mensaje que se desea guardar
     * @throws IllegalArgumentException si el mensaje no es válido
     * @throws NoSuchElementException   si no se puede encontrar un elemento
     * @throws DataAccessException      si ocurre un error en la base de datos
     * @throws JsonProcessingException  si ocurre un error al parsear el JSON
     * @throws RuntimeException         si ocurre un error inesperado
     * 
     */
    public void guardaMensaje(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Parse JSON
            MensajeChatDTO dto = objectMapper.readValue(message, MensajeChatDTO.class);

            // Obtener ID de respuesta si existe
            String respId = null;
            if (dto.getRespuesta() != null) {
                respId = dto.getRespuesta().getId_mensaje();
                if (respId == null) {
                    throw new IllegalArgumentException("ID de respuesta nulo en MensajeChatDTO");
                }
            }

            // Crear y guardar el mensaje principal
            MensajeChat mensaje = new MensajeChat(
                    null,
                    dto.getId_curso(),
                    dto.getId_clase(),
                    dto.getId_usuario(),
                    respId,
                    dto.getPregunta(),
                    dto.getMensaje(),
                    dto.getFecha());
            MensajeChat savedMessage = mensajeChatRepo.save(mensaje);
            String savedId = savedMessage.getId();

            // Actualizar CursoChat
            CursoChat cursoChat = Optional.ofNullable(cursoChatRepo.findByIdCurso(dto.getId_curso()))
                    .orElseThrow(() -> new NoSuchElementException(
                            "CursoChat no encontrado para idCurso: " + dto.getId_curso()));
            if (dto.getId_clase() == null || dto.getId_clase() == 0) {
                cursoChat.getMensajes().add(savedId);
            } else {
                cursoChat.getClases().stream()
                        .filter(c -> Objects.equals(c.getIdClase(), dto.getId_clase()))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException(
                                "ClaseChat no encontrada para idClase: " + dto.getId_clase()))
                        .getMensajes().add(savedId);
            }
            cursoChat.setUltimo(savedId);
            cursoChatRepo.save(cursoChat);

            // Actualizar UsuarioChat
            UsuarioChat userChat = Optional.ofNullable(usuarioChatRepo.findByIdUsuario(dto.getId_usuario()))
                    .orElseThrow(() -> new NoSuchElementException(
                            "UsuarioChat no encontrado para idUsuario: " + dto.getId_usuario()));
            if (userChat.getCursos().stream().noneMatch(id -> Objects.equals(id, cursoChat.getId()))) {
                userChat.getCursos().add(cursoChat.getId());
            }
            userChat.getMensajes().add(savedId);
            usuarioChatRepo.save(userChat);

            // Notificar profesores si es pregunta
            if (dto.getPregunta() != null) {
                Curso curso = cursoRepo.findById(dto.getId_curso())
                        .orElseThrow(
                                () -> new NoSuchElementException("Curso no encontrado con id: " + dto.getId_curso()));
                for (Usuario prof : curso.getProfesores_curso()) {
                    UsuarioChat profChat = Optional.ofNullable(usuarioChatRepo.findByIdUsuario(prof.getId_usuario()))
                            .orElseThrow(() -> new NoSuchElementException(
                                    "UsuarioChat no encontrado para profesor id: " + prof.getId_usuario()));
                    profChat.getMensajes().add(savedId);
                    usuarioChatRepo.save(profChat);
                }
            }

        } catch (JsonProcessingException e) {
            System.err.println("Error al parsear JSON del mensaje: " + e.getMessage());
            throw new IllegalArgumentException("Formato JSON inválido: " + e.getOriginalMessage(), e);
        } catch (DataAccessException e) {
            System.err.println("Error en la base de datos al guardar datos: " + e.getMessage());
            throw new IllegalStateException("Error interno de base de datos", e);
        } catch (NoSuchElementException e) {
            System.err.println(e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Error inesperado al guardar mensaje: " + e.getMessage());
            throw new RuntimeException("Error inesperado al procesar el mensaje", e);
        }
    }

    /**
     * Función para crear un usuario en el chat
     * 
     * @param message
     * @throws RuntimeException
     */
    public void creaUsuarioChat(String message) {
        // Crear una instancia de ObjectMapper para parsear el JSON
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Convertir el JSON en un objeto Usuario
            Usuario usuario = objectMapper.readValue(message, Usuario.class);
            if (usuarioChatRepo.findByIdUsuario(usuario.getId_usuario()) != null) {
                throw new RuntimeException("Ya existe un usuario con ese ID");
            }
            UsuarioChat usuarioChat = new UsuarioChat(
                    null, // String id
                    usuario.getId_usuario(), // Long id_usuario
                    new ArrayList<String>(), // List<CursoChat> cursos
                    new ArrayList<String>()); // List<String> mensajes
            usuarioChatRepo.save(usuarioChat);
        } catch (JsonProcessingException e) {
            System.err.println("Error convirtiendo JSON a objeto: " + e.getMessage());
            throw new RuntimeException("Error convirtiendo JSON a objeto: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error en crear el usuario del chat: " + e.getMessage());
            throw new RuntimeException("Error en crear el usuario del chat: " + e.getMessage());
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
            System.err.println("Error en crear chat del curso: " + e.getMessage());
            throw new RuntimeException("Error en crear chat del curso: " + e.getMessage());
        }
    }

    public void creaClaseChat(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Configurar ObjectMapper para manejar referencias circulares
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            Clase clase = objectMapper.readValue(message, Clase.class);
            ClaseChat claseChat = new ClaseChat(
                    clase.getId_clase(),
                    clase.getCurso_clase().getId_curso(),
                    new ArrayList<String>());

            CursoChat cursoChat = cursoChatRepo.findByIdCurso(clase.getCurso_clase().getId_curso());
            if (cursoChat == null) {
                System.err.println("Error: No se encontró el curso con ID " + clase.getCurso_clase().getId_curso());
                System.err.println("idCurso: " + clase.getCurso_clase().getId_curso());
                return;
            }
            List<ClaseChat> clasesChat = cursoChat.getClases();
            // Comprueba si clasesChat ya contiene una clase con el mismo id
            boolean exists = clasesChat.stream().anyMatch(c -> c.getIdClase() == clase.getId_clase());
            if (!exists) {
                clasesChat.add(claseChat);
                cursoChat.setClases(clasesChat);
                cursoChatRepo.save(cursoChat);
            }
        } catch (Exception e) {
            System.err.println("Error en crear el chat de la clase: " + e.getMessage());
            throw new RuntimeException("Error en crear el chat de la clase: " + e.getMessage());
        }
    }

    public void mensajeLeido(String message) {
        try {
            String[] lMex = message.replaceAll("\"", "").split(",");
            UsuarioChat usuario = this.usuarioChatRepo.findByIdUsuario(Long.parseLong(lMex[0]));
            if (usuario != null) {
                List<String> mensajes = usuario.getMensajes();
                mensajes.remove(lMex[1]);
                usuario.setMensajes(mensajes);
                this.usuarioChatRepo.save(usuario);
            }
        } catch (Exception e) {
            System.err.println("Error en el websocket de leer mensaje: " + e.getMessage());
            throw new RuntimeException("Error en el websocket de leer mensaje: " + e.getMessage());
        }
    }

    public void borrarClaseChat(Long idCurso, Long idClase) {
        try {
            CursoChat cursoChat = this.cursoChatRepo.findByIdCurso(idCurso);
            if (cursoChat != null) {
                List<ClaseChat> clases = cursoChat.getClases();
                for (ClaseChat clase : clases) {
                    if (clase.getIdClase() == idClase) {
                        clases.remove(clase);
                        break;
                    }
                }
                cursoChat.setClases(clases);
                this.cursoChatRepo.save(cursoChat);
            } else {
                System.err.println("No se pudo borrar la clase del chat, el curso no existe");
                throw new EntityNotFoundException("No se pudo borrar la clase del chat, el curso no existe");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error al borrar la clase del chat: " + e.getMessage());
            throw new RuntimeException("Error al borrar la clase del chat: " + e.getMessage());
        }
    }

    public void borrarCursoChat(Long idCurso) {
        try {
            CursoChat cursoChat = this.cursoChatRepo.findByIdCurso(idCurso);
            if (cursoChat != null) {
                this.cursoChatRepo.delete(cursoChat);
            }
        } catch (Exception e) {
            System.err.println("Error en borrar el chat del curso: " + e.getMessage());
            throw new RuntimeException("Error en borrar el chat del curso: " + e.getMessage());
        }
    }

    public void refreshTokenInOpenWebsocket(String sessionId, String newToken) {
        try {
            Authentication newAuth = jwtUtil.createAuthenticationFromToken(newToken);

            // Obtener el contexto actual y actualizar la autenticación
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(newAuth);

            // También actualizar en el accessor para que se conserve
            SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
            accessor.setSessionId(sessionId);
            accessor.setUser(newAuth);

            System.out.println("🔄 Token refrescado con éxito para sesión: " + sessionId);

        } catch (AuthenticationException e) {
            System.err.println("❌ Error al refrescar el token: " + e.getMessage());
            throw new RuntimeException("Error al refrescar el token: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error en el websocket de refrescar el token: " + e.getMessage());
            throw new RuntimeException("Error en el websocket de refrescar el token: " + e.getMessage());
        }
    }
}
