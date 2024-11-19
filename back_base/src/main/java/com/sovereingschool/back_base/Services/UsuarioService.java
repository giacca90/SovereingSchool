package com.sovereingschool.back_base.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.sovereingschool.back_base.DTOs.NewUsuario;
import com.sovereingschool.back_base.Interfaces.IUsuarioService;
import com.sovereingschool.back_base.Models.Curso;
import com.sovereingschool.back_base.Models.Login;
import com.sovereingschool.back_base.Models.Plan;
import com.sovereingschool.back_base.Models.Usuario;
import com.sovereingschool.back_base.Repositories.LoginRepository;
import com.sovereingschool.back_base.Repositories.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@Transactional
public class UsuarioService implements IUsuarioService {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private LoginRepository loginRepo;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @PersistenceContext
    private EntityManager entityManager;

    private String uploadDir = "/home/matt/Escritorio/Proyectos/SovereingSchool/Fotos";
    private final String backChatURL = "http://localhost:8070";

    @Override
    public String createUsuario(NewUsuario new_usuario) {
        Usuario usuario = new Usuario(
                null, // Long id_usuario
                new_usuario.getNombre_usuario(), // String nombre_usuario
                new_usuario.getFoto_usuario(), // List<String> foto_usuario
                null, // Strting presentación
                2, // Integer rol_usuario
                new_usuario.getPlan_usuario(), // Plan plan_usuario
                new_usuario.getCursos_usuario(), // List<String> cursos_usuario
                new_usuario.getFecha_registro_usuario()); // Date fecha_registro_usuario
        Usuario usuarioInsertado = this.repo.save(usuario);
        if (usuarioInsertado.getId_usuario() == null) {
            return "Error en crear el usuario";
        }
        Login login = new Login();
        login.setUsuario(usuarioInsertado);
        login.setCorreo_electronico(new_usuario.getCorreo_electronico());
        login.setPassword(new_usuario.getPassword());
        this.loginRepo.save(login);

        try {
            WebClient webClient = WebClient.create(backChatURL);
            webClient.post().uri("/crea_usuario_chat")
                    .body(Mono.just(usuarioInsertado), Usuario.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(e -> {
                        // Manejo de errores
                        System.err.println("ERROR: " + e.getMessage());
                        e.printStackTrace();
                    }).subscribe(res -> {
                        // Maneja el resultado cuando esté disponible
                        if (res != null && res.equals("Usuario chat creado con exito!!!")) {
                            System.out.println("Usuario chat creado con éxito!!!");
                        } else {
                            System.err.println("Error en crear el usuario en el chat");
                        }
                    });
        } catch (Exception e) {
            System.err.println("Error en crear el usuario en el chat: " + e.getMessage());
        }

        sendDataToStream(usuarioInsertado, 0).subscribe(resp -> {
            System.out.println("Respuesta del segundo microservicio: " + resp);
        }, error -> {
            System.err.println("Error al comunicarse con el segundo microservicio: " + error.getMessage());
        });
        return "Usuario creado con éxito!!!";
    };

    @Override
    public Usuario getUsuario(Long id_usuario) {
        return this.repo.findUsuarioForId(id_usuario);
    }

    @Override
    public String getNombreUsuario(Long id_usuario) {
        return this.repo.findNombreUsuarioForId(id_usuario);
    }

    @Override
    public List<String> getFotosUsuario(Long id_usuario) {
        Usuario usuario = this.repo.findUsuarioForId(id_usuario);
        if (usuario == null)
            return null;
        return usuario.getFoto_usuario();
    }

    @Override
    public Integer getRollUsuario(Long id_usuario) {
        return this.repo.findRollUsuarioForId(id_usuario);
    }

    @Override
    public Plan getPlanUsuario(Long id_usuario) {
        return this.repo.findPlanUsuarioForId(id_usuario);
    }

    @Override
    public List<Curso> getCursosUsuario(Long id_usuario) {
        Usuario usuario = this.repo.findUsuarioForId(id_usuario);
        if (usuario == null)
            return null;
        return usuario.getCursos_usuario();
    }

    @Override
    public Usuario updateUsuario(Usuario usuario) {
        Usuario usuario_old = this.getUsuario(usuario.getId_usuario());
        System.out.println("PRUEBA: " + usuario.getFoto_usuario().toString());
        System.out.println("PRUEBA2: " + usuario.getFoto_usuario().size());
        for (String foto : usuario.getFoto_usuario()) {
            System.out.println("PRUEBA3: " + foto);
        }

        for (String foto : usuario_old.getFoto_usuario()) { // TODO: revisar si esto es necesario
            if (!usuario.getFoto_usuario().contains(foto)) {
                Path photoPath = Paths.get(uploadDir, foto.substring(foto.lastIndexOf("/")));
                try {
                    if (Files.exists(photoPath)) {
                        Files.delete(photoPath);
                        System.out.println("Foto eliminada: " + photoPath.toString());
                    } else {
                        System.out.println("Foto no encontrada: " + photoPath.toString());
                    }
                } catch (IOException e) {
                    System.err.println("Error al eliminar la foto: " + photoPath.toString());
                    e.printStackTrace();
                }
            }
        }
        return this.repo.save(usuario);
    }

    @Override
    public Integer changePlanUsuario(Usuario usuario) {
        return this.repo.changePlanUsuarioForId(usuario.getId_usuario(), usuario.getPlan_usuario());
    }

    @Override
    public Integer changeCursosUsuario(Usuario usuario) {
        Usuario old_usuario = this.repo.findUsuarioForId(usuario.getId_usuario());
        if (old_usuario == null)
            return 0;
        old_usuario.setCursos_usuario(usuario.getCursos_usuario());

        sendDataToStream(old_usuario, 1).subscribe(response -> {
            System.out.println("Respuesta del segundo microservicio: " + response);
        }, error -> {
            System.err.println("Error al comunicarse con el segundo microservicio: " + error.getMessage());
        });

        return this.repo.changeUsuarioForId(usuario.getId_usuario(), old_usuario);
    }

    @Override
    public String deleteUsuario(Long id) {
        if (this.repo.findUsuarioForId(id) == null) {
            return null;
        }
        this.loginRepo.deleteById(id);
        this.repo.deleteById(id);
        return "Usuario eliminado con éxito!!!";
    }

    @Override
    public List<Usuario> getProfes() {
        return this.repo.findProfes();
    }

    private <D> Mono<String> sendDataToStream(D data, Integer tipo) {

        if (tipo == 0) {

            WebClient webClient = webClientBuilder.baseUrl("http://localhost:8090").build();
            return webClient.post().uri("/nuevoUsuario").body(Mono.just(data), Usuario.class).retrieve()
                    .bodyToMono(String.class).retryWhen(Retry.backoff(3, Duration.ofSeconds(5)).filter(throwable -> {
                        if (throwable instanceof WebClientResponseException) {
                            WebClientResponseException e = (WebClientResponseException) throwable;
                            return e.getStatusCode().value() == 404;
                        }
                        return false;
                    })).doOnError(throwable -> {
                        // Manejo del error final después de reintentos
                        System.err.println("Error en la petición después de reintentar: " + throwable.getMessage());
                    });
        } else if (tipo == 1) {

            WebClient webClient = webClientBuilder.baseUrl("http://localhost:8090").build();
            return webClient.put().uri("/nuevoCursoUsuario").body(Mono.just(data), Usuario.class).retrieve()
                    .bodyToMono(String.class).retryWhen(Retry.backoff(3, Duration.ofSeconds(5)).filter(throwable -> {
                        if (throwable instanceof WebClientResponseException) {
                            WebClientResponseException e = (WebClientResponseException) throwable;
                            return e.getStatusCode().value() == 404;
                        }
                        return false;
                    })).doOnError(throwable -> {
                        // Manejo del error final después de reintentos
                        System.err.println("Error en la petición después de reintentar: " + throwable.getMessage());
                    });

        } else
            // Manejo del caso de error para tipos no soportados
            return Mono.error(new IllegalArgumentException("Tipo no soportado: " + tipo));
    }
}
