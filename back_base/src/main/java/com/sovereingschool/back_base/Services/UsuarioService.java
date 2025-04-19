package com.sovereingschool.back_base.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.sovereingschool.back_base.DTOs.AuthResponse;
import com.sovereingschool.back_base.DTOs.NewUsuario;
import com.sovereingschool.back_base.Interfaces.IUsuarioService;
import com.sovereingschool.back_base.Models.Curso;
import com.sovereingschool.back_base.Models.Login;
import com.sovereingschool.back_base.Models.Plan;
import com.sovereingschool.back_base.Models.RoleEnum;
import com.sovereingschool.back_base.Models.Usuario;
import com.sovereingschool.back_base.Repositories.LoginRepository;
import com.sovereingschool.back_base.Repositories.UsuarioRepository;
import com.sovereingschool.back_base.Utils.JwtUtil;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service
@Transactional
public class UsuarioService implements IUsuarioService {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private LoginRepository loginRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @PersistenceContext
    private EntityManager entityManager;

    private String uploadDir = "/home/matt/Escritorio/Proyectos/SovereingSchool/Fotos";
    private final String backChatURL = "https://localhost:8070";
    private final String backStreamURL = "https://localhost:8090";

    UsuarioService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse createUsuario(NewUsuario new_usuario) {
        Usuario usuario = new Usuario(
                null, // Long id_usuario
                new_usuario.getNombre_usuario(), // String nombre_usuario
                new_usuario.getFoto_usuario(), // List<String> foto_usuario
                null, // Strting presentación
                RoleEnum.USER, // Integer rol_usuario
                new_usuario.getPlan_usuario(), // Plan plan_usuario
                new_usuario.getCursos_usuario(), // List<String> cursos_usuario
                new_usuario.getFecha_registro_usuario(), // Date fecha_registro_usuario
                true,
                true,
                true,
                true);
        Usuario usuarioInsertado = this.repo.save(usuario);
        if (usuarioInsertado.getId_usuario() == null) {
            throw new RuntimeException("Error al crear el usuario");
        }
        Login login = new Login();
        login.setUsuario(usuarioInsertado);
        login.setCorreo_electronico(new_usuario.getCorreo_electronico());
        login.setPassword(passwordEncoder.encode(new_usuario.getPassword()));
        this.loginRepo.save(login);

        // Crear el usuario en el microservicio de chat
        try {
            WebClient webClient = createSecureWebClient(backChatURL);
            webClient.post().uri("/crea_usuario_chat")
                    .body(Mono.just(usuarioInsertado), Usuario.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(e -> {
                        // Manejo de errores
                        System.err.println("Error al conectar con el microservicio de chat: " + e.getMessage());
                        e.printStackTrace();
                    }).subscribe(res -> {
                        // Maneja el resultado cuando esté disponible
                        if (res != null && res.equals("Usuario chat creado con exito!!!")) {
                        } else {
                            System.err.println("Error en crear el usuario en el chat:");
                            System.err.println(res);
                        }
                    });
        } catch (Exception e) {
            System.err.println("Error en crear el usuario en el chat: " + e.getMessage());
        }

        // Crear el usuario en el microservicio de stream
        try {
            WebClient webClientStream = createSecureWebClient(backStreamURL);
            webClientStream.put().uri("/nuevoUsuario")
                    .body(Mono.just(usuarioInsertado), Usuario.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(e -> {
                        // Manejo de errores
                        System.err.println("Error al conectar con el microservicio de stream: " + e.getMessage());
                        e.printStackTrace();
                    }).subscribe(res -> {
                        // Maneja el resultado cuando esté disponible
                        if (res != null && res.equals("Usuario creado con exito!!!")) {
                        } else {
                            System.err.println("Error en crear el usuario en el stream:");
                            System.err.println(res);
                        }
                    });
        } catch (Exception e) {
            System.err.println("Error en crear el usuario en el stream: " + e.getMessage());
        }

        // Creamos la respuesta con JWT
        List<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority("ROLE_" + usuarioInsertado.getRoll_usuario().name()));
        UserDetails userDetails = new User(usuarioInsertado.getNombre_usuario(),
                login.getPassword(),
                usuarioInsertado.getIsEnabled(),
                usuarioInsertado.getAccountNoExpired(),
                usuarioInsertado.getCredentialsNoExpired(),
                usuarioInsertado.getAccountNoLocked(),
                roles);

        Authentication auth = new UsernamePasswordAuthenticationToken(new_usuario.getCorreo_electronico(),
                userDetails.getPassword(),
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
        String accessToken = jwtUtil.generateToken(auth);

        return new AuthResponse(true, "Usuario creado con éxito", usuarioInsertado, accessToken);

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
    public RoleEnum getRollUsuario(Long id_usuario) {
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

        for (String foto : usuario_old.getFoto_usuario()) {
            if (!usuario.getFoto_usuario().contains(foto)) {
                Path photoPath = Paths.get(uploadDir, foto.substring(foto.lastIndexOf("/")));
                try {
                    if (Files.exists(photoPath)) {
                        Files.delete(photoPath);
                    } else {
                        System.out.println("La foto no existe: " + photoPath.toString());
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

        try {
            // Añadir el usuario al microservicio de stream
            WebClient webClientStream = createSecureWebClient(backStreamURL);
            webClientStream.put().uri("/nuevoCursoUsuario")
                    .body(Mono.just(old_usuario), Usuario.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(e -> {
                        // Manejo de errores
                        System.err.println("Error al conectar con el microservicio de stream: " + e.getMessage());
                        e.printStackTrace();
                    }).subscribe(res -> {
                        // Maneja el resultado cuando esté disponible
                        if (res != null && res.equals("Usuario creado con exito!!!")) {
                        } else {
                            System.err.println("Error en crear el usuario en el stream:");
                            System.err.println(res);
                        }
                    });
        } catch (Exception e) {
            System.err.println("Error en crear el usuario en el stream: " + e.getMessage());
        }

        // Añadir el usuario al microservicio de chat
        // TODO: Implementar la lógica para añadir el usuario al microservicio de chat
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

    public WebClient createSecureWebClient(String baseUrl) throws Exception {
        // Crear un SslContext que confía en todos los certificados (incluidos
        // autofirmados)
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        // Configurar HttpClient con el contexto SSL
        HttpClient httpClient = HttpClient.create()
                .secure(spec -> spec.sslContext(sslContext));

        // Obtener token
        String authToken = this.jwtUtil.generateTokenForServer();

        // Conectar HttpClient con WebClient y añadir header por defecto
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + authToken)
                .build();
    }
}
