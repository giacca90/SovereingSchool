package com.sovereingschool.back_chat.Services;

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
import com.sovereingschool.back_chat.DTOs.InitChatDTO;
import com.sovereingschool.back_chat.Models.CursoChat;
import com.sovereingschool.back_chat.Models.MensajeChat;
import com.sovereingschool.back_chat.Models.UsuarioChat;
import com.sovereingschool.back_chat.Repositories.UsuarioChatRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class InitChatService {
    @Autowired
    private UsuarioChatRepository usuarioRepo;

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
            UsuarioChat usuarioChat = this.usuarioRepo.findByIdUsuario(idUsuario);
            System.out.println("USUARIOCHAT: " + usuarioChat);
            if (usuarioChat == null) {
                usuarioChat = new UsuarioChat(null, 0L, null, null); // Objeto por defecto si no se encuentra
            }
            this.startWatching(idUsuario);
            return new InitChatDTO(usuarioChat.getIdUsuario(), usuarioChat.getMensajes(), usuarioChat.getCursos());
        } catch (Exception e) {
            // Manejo de excepciones
            System.err.println("Error al buscar usuario: " + e.getMessage());
            UsuarioChat usuarioChat = new UsuarioChat(null, 0L, null, null); // Objeto por defecto si no se encuentra
            return new InitChatDTO(usuarioChat.getIdUsuario(), usuarioChat.getMensajes(), usuarioChat.getCursos());
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
            InitChatDTO updateDTO = new InitChatDTO(
                    document.getLong("idUsuario"),
                    document.getList("mensajes", MensajeChat.class),
                    document.getList("cursos", CursoChat.class));
            simpMessagingTemplate.convertAndSend("/init_chat/result", updateDTO);
        } catch (Exception e) {
            System.err.println("Error al notificar al frontend: " + e.getMessage());
        }
    }
}
