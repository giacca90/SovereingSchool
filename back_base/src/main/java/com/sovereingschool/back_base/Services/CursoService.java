package com.sovereingschool.back_base.Services;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.sovereingschool.back_base.Interfaces.ICursoService;
import com.sovereingschool.back_base.Models.Clase;
import com.sovereingschool.back_base.Models.Curso;
import com.sovereingschool.back_base.Models.Plan;
import com.sovereingschool.back_base.Models.Usuario;
import com.sovereingschool.back_base.Repositories.ClaseRepository;
import com.sovereingschool.back_base.Repositories.CursoRepository;

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
public class CursoService implements ICursoService {

    @Autowired
    private CursoRepository repo;

    @Autowired
    private ClaseRepository claseRepo;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @PersistenceContext
    private EntityManager entityManager;
    private final String uploadDir = "/home/matt/Escritorio/Proyectos/SovereingSchool/Videos";
    private final Path baseUploadDir = Paths.get(uploadDir); // Directorio base para subir videos
    private final String backStreamURL = "https://localhost:8090";

    private final String backChatURL = "https://localhost:8070";

    @Override
    public Long createCurso(Curso new_curso) {
        new_curso.setId_curso(null);
        Curso res = this.repo.save(new_curso);
        return res.getId_curso();
    }

    @Override
    public Curso getCurso(Long id_curso) {
        Optional<Curso> curso = this.repo.findById(id_curso);
        if (curso.isPresent()) {
            return curso.get();
        }
        return null;
    }

    @Override
    public String getNombreCurso(Long id_curso) {
        return this.repo.findNombreCursoById(id_curso);
    }

    @Override
    public List<Usuario> getProfesoresCurso(Long id_curso) {
        return this.repo.findProfesoresCursoById(id_curso);
    }

    @Override
    public Date getFechaCreacionCurso(Long id_curso) {
        return this.repo.findFechaCreacionCursoById(id_curso);
    }

    @Override
    public List<Clase> getClasesDelCurso(Long id_curso) {
        return this.repo.findClasesCursoById(id_curso);
    }

    @Override
    public List<Plan> getPlanesDelCurso(Long id_curso) {
        return this.repo.findPlanesCursoById(id_curso);
    }

    @Override
    public BigDecimal getPrecioCurso(Long id_curso) {
        return this.repo.findPrecioCursoById(id_curso);
    }

    @Override
    public Curso updateCurso(Curso curso) {
        List<Clase> clases = curso.getClases_curso();
        curso.setClases_curso(null);
        if (curso.getId_curso().equals(0L)) {
            curso.setId_curso(null);
            curso = this.repo.save(curso);

            try {
                WebClient webClient = WebClient.create(backChatURL);
                webClient.post().uri("/crea_curso_chat")
                        .body(Mono.just(curso), Curso.class)
                        .retrieve()
                        .bodyToMono(String.class)
                        .doOnError(e -> {
                            // Manejo de errores
                            System.err.println("ERROR: " + e.getMessage());
                            e.printStackTrace();
                        }).subscribe(res -> {
                            // Maneja el resultado cuando esté disponible
                            if (res != null && res.equals("Curso chat creado con exito!!!")) {
                                System.out.println("Curso chat creado con éxito!!!");
                            } else {
                                System.err.println("Error en crear el curso en el chat");
                            }
                        });
            } catch (Exception e) {
                System.err.println("Error en crear el curso: " + e.getMessage());
                return null;
            }
        }
        Path cursoPath = baseUploadDir.resolve(curso.getId_curso().toString());
        File cursoFile = new File(cursoPath.toString());
        if (!cursoFile.exists() || !cursoFile.isDirectory()) {
            if (!cursoFile.mkdir()) {
                System.err.println("Error en crear la carpeta del curso.");
                return null;
            }
        }
        if (clases.size() > 0) {
            for (Clase clase : clases) {
                clase.setCurso_clase(curso);
                if (clase.getId_clase().equals(0L)) {
                    clase.setId_clase(null);
                }
                try {
                    clase = this.claseRepo.save(clase);
                } catch (Exception e) {
                    System.err.println("Error en guardar la clase: " + e.getMessage());
                    return null;
                }
                Path clasePath = cursoPath.resolve(clase.getId_clase().toString());
                File claseFile = new File(clasePath.toString());
                if (!claseFile.exists() || !claseFile.isDirectory()) {
                    if (!claseFile.mkdir()) {
                        return null;
                    }
                }
            }
            curso.setClases_curso(clases);
            try {
                curso = this.repo.save(curso);
            } catch (Exception e) {
                System.err.println("Error en actualizar el curso: " + e.getMessage());
                return null;
            }
        }
        try {
            WebClient webClient = createSecureWebClient(backStreamURL);
            webClient.post().uri("/convertir_videos")
                    .body(Mono.just(curso), Curso.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(e -> {
                        // Manejo de errores
                        System.err.println("ERROR: " + e.getMessage());
                        e.printStackTrace();
                    }).subscribe(res -> {
                        // Maneja el resultado cuando esté disponible
                        if (res != null && res.equals("Videos convertidos con éxito!!!")) {
                            System.out.println("Videos convertidos con éxito!!!");
                        } else {
                            System.err.println("Error en convertir los videos del curso");
                        }
                    });
        } catch (Exception e) {
            System.err.println("Error en crear el curso: " + e.getMessage());
            return null;
        }
        return curso;
    }

    @Override
    public Boolean deleteCurso(Long id_curso) {
        if (!this.repo.findById(id_curso).isPresent()) {
            return false;
        }
        if (this.getCurso(id_curso).getClases_curso() != null) {
            for (Clase clase : this.getCurso(id_curso).getClases_curso()) {
                this.deleteClase(clase);
            }
        }
        this.repo.deleteById(id_curso);

        Path cursoPath = Paths.get(this.baseUploadDir.toString(), id_curso.toString());
        File cursoFile = new File(cursoPath.toString());
        if (cursoFile.exists()) {
            try {

                Files.walkFileTree(cursoPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
                System.out.println("Carpeta eliminada exitosamente.");
            } catch (Exception e) {
                System.err.println("Error en borrar la carpeta del curso: " + e.getMessage());
            }
        } else {
            System.out.println("La carpeta no existe.");
        }

        WebClient webClient = webClientBuilder.baseUrl(backStreamURL)
                .build();
        webClient.delete().uri("/deleteCurso/" + id_curso).retrieve().bodyToMono(Boolean.class).doOnError(e -> {
            // Manejo de errores
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }).subscribe(res -> {
            // Maneja el resultado cuando esté disponible
            if (res != null && res) {
                System.out.println("Curso borrado con éxito!!!");
            } else {
                System.err.println("Error en borrar el curso en el servicio de reproducción");
            }
        });

        return true;
    }

    @Override
    public List<Curso> getAll() {
        return this.repo.findAll();
    }

    @Override
    public void deleteClase(Clase clase) {
        Optional<Clase> optionalClase = this.claseRepo.findById(clase.getId_clase());
        if (optionalClase.isPresent()) {
            this.claseRepo.delete(clase);
            if (clase.getDireccion_clase().length() > 0) {
                try {
                    Path path = Paths.get(clase.getDireccion_clase()).getParent();
                    if (Files.exists(path)) {
                        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                Files.delete(file);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                Files.delete(dir);
                                return FileVisitResult.CONTINUE;
                            }
                        });
                        System.out.println("Carpeta eliminada exitosamente.");
                    } else {
                        System.out.println("La carpeta no existe.");
                    }
                } catch (Exception e) {
                    System.err.println("Error en borrar el video: " + e.getMessage());
                }
            }

            try {
                WebClient webClient = webClientBuilder.baseUrl(backStreamURL).build();
                webClient.delete()
                        .uri("/deleteClase/" + clase.getCurso_clase().getId_curso().toString() + "/"
                                + clase.getId_clase().toString())
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .doOnError(e -> {
                            // Manejo de errores
                            System.err.println("ERROR: " + e.getMessage());
                            e.printStackTrace();
                        }).subscribe(res -> {
                            // Maneja el resultado cuando esté disponible
                            if (res != null && res) {
                                System.out.println("Actualización exitosa");
                            } else {
                                System.err.println("Error en actualizar el curso en el servicio de reproducción");
                            }
                        });
            } catch (Exception e) {
                System.err.println("error en borrar la clase en el streaming: " + e.getMessage());
            }
        } else {
            System.err.println("Clase no encontrada con ID: " + clase.getId_clase());
        }
    }

    @Override
    public String subeVideo(MultipartFile file) {
        try {
            // Obtener el nombre original del archivo o usar un valor predeterminado si es
            // null
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null) {
                originalFileName = "unknown_file";
            }

            // Define el path para guardar el archivo subido
            String cleanedFileName = StringUtils.cleanPath(originalFileName);
            Path filePath = baseUploadDir.resolve(UUID.randomUUID().toString() + "_" + cleanedFileName);

            // Guarda el archivo en el servidor
            Files.write(filePath, file.getBytes());
            return filePath.normalize().toString();

        } catch (Exception e) {
            System.err.println("Error en subir el video: " + e.getMessage());
            return null;
        }
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

        // Conectar HttpClient con WebClient
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl) // Establecer URL base
                .build();
    }
}