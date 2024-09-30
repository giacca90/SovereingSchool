package com.sovereingschool.back_chat.Services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sovereingschool.back_chat.DTOs.ClaseChatDTO;
import com.sovereingschool.back_chat.DTOs.CursoChatDTO;
import com.sovereingschool.back_chat.DTOs.MensajeChatDTO;
import com.sovereingschool.back_chat.Models.ClaseChat;
import com.sovereingschool.back_chat.Models.CursoChat;
import com.sovereingschool.back_chat.Models.MensajeChat;
import com.sovereingschool.back_chat.Repositories.ClaseRepository;
import com.sovereingschool.back_chat.Repositories.CursoChatRepository;
import com.sovereingschool.back_chat.Repositories.CursoRepository;
import com.sovereingschool.back_chat.Repositories.MensajeChatRepository;
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
                        for (String mensajeId : mensajes) {
                            MensajeChat mensaje = mensajeChatRepo.findById(mensajeId).get();
                            MensajeChatDTO mensajeDTO = new MensajeChatDTO(
                                    mensaje.getIdMensaje(),
                                    mensaje.getIdCurso(),
                                    mensaje.getIdClase(),
                                    mensaje.getIdUsuario(),
                                    null,
                                    null,
                                    usuarioRepo.findNombreUsuarioForId(mensaje.getIdUsuario()),
                                    null,
                                    mensaje.getMensaje(),
                                    null,
                                    mensajeId,
                                    mensaje.getFecha());
                            mensajesDTO.add(mensajeDTO);
                        }
                    }
                }
            }

            List<String> mensajes = cursoChat.getMensajes();
            List<MensajeChatDTO> mensajesDTO = new ArrayList<>();
            if (mensajes != null && mensajes.size() > 0) {
                for (String mensajeId : mensajes) {
                    MensajeChat mensaje = mensajeChatRepo.findById(mensajeId).get();
                    MensajeChatDTO mensajeDTO = new MensajeChatDTO(
                            mensaje.getIdMensaje(),
                            mensaje.getIdCurso(),
                            mensaje.getIdClase(),
                            mensaje.getIdUsuario(),
                            cursoRepo.findNombreCursoById(mensaje.getIdCurso()),
                            claseRepo.findNombreClaseById(mensaje.getIdClase()),
                            null,
                            null,
                            mensaje.getMensaje(),
                            null,
                            mensajeId,
                            mensaje.getFecha());
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

        return cursoChatDTO;
    }

}
