package com.sovereingschool.back_base.Services;

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

    @Override
    public String createUsuario(NewUsuario new_usuario) {
        Usuario usuario = new Usuario();
        usuario.setNombre_usuario(new_usuario.getNombre_usuario());
        usuario.setFoto_usuario(new_usuario.getFoto_usuario());
        usuario.setRoll_usuario(2);
        usuario.setPlan_usuario(new_usuario.getPlan_usuario());
        usuario.setCursos_usuario(new_usuario.getCursos_usuario());
        usuario.setFecha_registro_usuario(new_usuario.getFecha_registro_usuario());
        Usuario usuarioInsertado = this.repo.save(usuario);
        if (usuarioInsertado.getId_usuario() == null)
            return "Error en crear el usuario";
        Login login = new Login();
        login.setUsuario(usuarioInsertado);
        login.setCorreo_electronico(new_usuario.getCorreo_electronico());
        login.setPassword(new_usuario.getPassword());
        this.loginRepo.save(login);

        sendDataToStream(usuarioInsertado, 0).subscribe(response -> {
            System.out.println("Respuesta del segundo microservicio: " + response);
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
    public Integer changeNombreUsuario(Usuario usuario) {
        return this.repo.changeNombreUsuarioForId(usuario.getId_usuario(), usuario.getNombre_usuario());
    }

    @Override
    public Integer changeFotosUsuario(Usuario usuario) {
        Usuario old_usuario = this.repo.findUsuarioForId(usuario.getId_usuario());
        if (old_usuario == null)
            return 0;
        old_usuario.setFoto_usuario(usuario.getFoto_usuario());
        return this.repo.changeUsuarioForId(usuario.getId_usuario(), old_usuario);
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
