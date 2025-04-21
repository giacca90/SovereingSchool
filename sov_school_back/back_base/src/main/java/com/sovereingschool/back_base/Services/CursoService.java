package com.sovereingschool.back_base.Services;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.sovereingschool.back_base.Interfaces.ICursoService;
import com.sovereingschool.back_base.Utils.JwtUtil;
import com.sovereingschool.back_common.Models.Clase;
import com.sovereingschool.back_common.Models.Curso;
import com.sovereingschool.back_common.Models.Plan;
import com.sovereingschool.back_common.Models.Usuario;
import com.sovereingschool.back_common.Repositories.ClaseRepository;
import com.sovereingschool.back_common.Repositories.CursoRepository;

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
    private JwtUtil jwtUtil;

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

    /**
     * Función para actualizar o crear un nuevo curso
     * 
     * @param curso Curso: curso a actualizar
     */
    @Override
    public Curso updateCurso(Curso curso) {
        List<Clase> clases = curso.getClases_curso();
        curso.setClases_curso(null);
        // Si el curso no existe, crear un nuevo
        if (curso.getId_curso().equals(0L)) {
            curso.setId_curso(null);
            curso = this.repo.save(curso);
            // Crea el chat del nuevo curso
            try {
                WebClient webClient = createSecureWebClient(backChatURL);
                webClient.post().uri("/crea_curso_chat")
                        .body(Mono.just(curso), Curso.class)
                        .retrieve()
                        .bodyToMono(String.class)
                        .doOnError(e -> {
                            System.err.println("Error al conectar con el microservicio de chat " + e.getMessage());
                        }).subscribe(res -> {
                            if (res == null || !res.equals("Curso chat creado con exito!!!")) {
                            } else {
                                System.err.println("Error en crear el curso en el chat");
                                System.err.println(res);
                            }
                        });
            } catch (Exception e) {
                System.err.println("Error en crear el curso: " + e.getMessage());
                return null;
            }
        }
        // Crear la carpeta del curso si no existe
        Path cursoPath = baseUploadDir.resolve(curso.getId_curso().toString());
        File cursoFile = new File(cursoPath.toString());
        if (!cursoFile.exists() || !cursoFile.isDirectory()) {
            if (!cursoFile.mkdir()) {
                System.err.println("Error en crear la carpeta del curso.");
                return null;
            }
        }
        // Crear las clases del curso si no existen
        if (clases.size() > 0) {
            for (Clase clase : clases) {
                clase.setCurso_clase(curso);
                if (clase.getId_clase().equals(0L)) {
                    clase.setId_clase(null);
                }
                // Comprueba si la clase es una emisión en directo
                if (clase.getDireccion_clase() != null && !clase.getDireccion_clase().contains("/")) {
                    clase.setDireccion_clase(this.uploadDir + "/" + clase.getDireccion_clase() + "/master.m3u8");
                } else if (clase.getDireccion_clase() != null && !clase.getDireccion_clase()
                        .contains("/" + curso.getId_curso() + "/" + clase.getId_clase() + "/")
                        && clase.getTipo_clase() > 0) {
                    this.moverCarpeta(clase.getDireccion_clase(),
                            this.uploadDir + "/" + curso.getId_curso() + "/" + clase.getId_clase());
                    clase.setDireccion_clase(
                            this.uploadDir + "/" + curso.getId_curso() + "/" + clase.getId_clase() + "/master.m3u8");
                }
                try {
                    clase = this.claseRepo.save(clase);
                } catch (Exception e) {
                    System.err.println("Error en guardar la clase: " + e.getMessage());
                    return null;
                }
                // Crea el chat de la clase si no existe
                try {
                    clase.setCurso_clase(curso); // Asegurarnos que la referencia al curso está establecida

                    // Crear una versión simplificada para enviar
                    Map<String, Object> claseData = new HashMap<>();
                    claseData.put("id_clase", clase.getId_clase());
                    claseData.put("curso_clase", Map.of("id_curso", curso.getId_curso()));

                    WebClient webClient = createSecureWebClient(backChatURL);
                    webClient.post().uri("/crea_clase_chat")
                            .body(Mono.just(claseData), Map.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .doOnError(e -> {
                                System.err.println("Error al crear el chat de la clase: " + e.getMessage());
                            }).subscribe(res -> {
                                if (res == null || !res.equals("Clase chat creado con exito!!!")) {
                                    System.err.println("Error en crear la clase en el chat: ");
                                    System.err.println(res);
                                }
                            });
                } catch (Exception e) {
                    System.err.println("Error al crear el chat de la clase: " + e.getMessage());
                    return null;
                }

                // Crea la carpeta de la clase si no existe
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
        // Convertir los videos del curso
        try {
            // Obtener token
            WebClient webClient = createSecureWebClient(backStreamURL);
            webClient.post().uri("/convertir_videos")
                    .body(Mono.just(curso), Curso.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(e -> {
                        // Manejo de errores
                        System.err.println("Error al conectar con el microservicio de streaming: " + e.getMessage());
                    }).subscribe(res -> {
                        // Maneja el resultado cuando esté disponible
                        if (res == null || !res.equals("Videos convertidos con éxito!!!")) {
                            System.err.println("Error en convertir los videos del curso");
                            System.err.println(res);
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
            } catch (Exception e) {
                System.err.println("Error en borrar la carpeta del curso: " + e.getMessage());
            }
        } else {
            System.err.println("La carpeta del curso no existe.");
        }

        // Eliminar el curso del microservicio de streaming
        try {
            // Obtener token
            WebClient webClient = createSecureWebClient(backStreamURL);
            webClient.delete()
                    .uri("/deleteCurso/" + id_curso)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .doOnError(e -> {
                        System.err.println("Error al conectar con el microservicio de streaming: " + e.getMessage());
                    }).subscribe(res -> {
                        if (res == null || !res) {
                            System.err.println("Error en borrar el curso en el servicio de reproducción");
                        }
                    });
        } catch (Exception e) {
            System.err.println("Error al conectar con el microservicio de streaming: " + e.getMessage());
        }

        // Eliminar el curso del microservicio de chat
        try {
            // Obtener token
            WebClient webClientChat = createSecureWebClient(backChatURL);
            webClientChat.delete()
                    .uri("/delete_curso_chat/" + id_curso)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(e -> {
                        System.err.println("Error al conectar con el microservicio de streaming: " + e.getMessage());
                    }).subscribe(res -> {
                        if (res == null || !res.equals("Curso chat borrado con exito!!!")) {
                            System.err.println("Error al eliminar el curso del microservicio de chat");
                            System.err.println(res);
                        }
                    });
        }

        catch (Exception e) {
            System.err.println("Error al conectar con el microservicio de streaming: " + e.getMessage());
        }

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
                    } else {
                        System.err.println("La carpeta de la clase no existe.");
                    }
                } catch (Exception e) {
                    System.err.println("Error en borrar el video: " + e.getMessage());
                }
            }

            // Eliminar la carpeta de la clase
            try {
                // Obtener token
                WebClient webClient = createSecureWebClient(backStreamURL);
                webClient.delete()
                        .uri("/deleteClase/" + clase.getCurso_clase().getId_curso().toString() + "/"
                                + clase.getId_clase().toString())
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .doOnError(e -> {
                            // Manejo de errores
                            System.err
                                    .println("Error al conectar con el microservicio de streaming: " + e.getMessage());
                        }).subscribe(res -> {
                            // Maneja el resultado cuando esté disponible
                            if (res == null || !res) {
                                System.err.println("Error en actualizar el curso en el servicio de reproducción");
                                System.err.println(res);
                            }
                        });
            } catch (Exception e) {
                System.err.println("error en borrar la clase en el streaming: " + e.getMessage());
            }

            // Elimina el chat de la clase
            try {
                // Obtener token
                WebClient webClient = createSecureWebClient(backChatURL);
                webClient.delete()
                        .uri("/delete_clase_chat/" + clase.getCurso_clase().getId_curso().toString() + "/"
                                + clase.getId_clase().toString())
                        .retrieve()
                        .bodyToMono(String.class)
                        .doOnError(e -> {
                            System.err.println("Error al conectar con el microservicio de chat: " + e.getMessage());
                        }).subscribe(res -> {
                            if (res == null || !res.equals("Clase chat borrado con exito!!!")) {
                                System.err.println("Error en borrar la clase en el chat");
                                System.err.println(res);
                            }
                        });
            } catch (Exception e) {
                System.err.println("Error en borrar la clase en el chat: " + e.getMessage());
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
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        // Configurar HttpClient con el contexto SSL
        HttpClient httpClient = HttpClient.create()
                .secure(spec -> spec.sslContext(sslContext));

        // Obtener token
        String authToken = this.jwtUtil.generateToken(null, "server");

        // Conectar HttpClient con WebClient y añadir header por defecto
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + authToken)
                .build();
    }

    /**
     * Función para mover una carpeta y todas sus sub-directories y archivos
     * 
     * @param origen  origen de la carpeta
     * @param destino destino de la carpeta
     */
    private void moverCarpeta(String origen, String destino) {
        Path origenPath = Paths.get(origen).getParent();
        Path destinoPath = Paths.get(destino);
        if (!Files.exists(origenPath)) {
            System.err.println("La carpeta origen no existe");
            return;
        }
        if (!Files.isDirectory(origenPath)) {
            System.err.println("El origen no es una carpeta");
            return;
        }
        try {
            Files.walkFileTree(origenPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetFile = destinoPath.resolve(origenPath.relativize(file));
                    Files.createDirectories(targetFile.getParent()); // Crea los directorios necesarios
                    Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Path targetDir = destinoPath.resolve(origenPath.relativize(dir));
                    Files.createDirectories(targetDir); // Crea el directorio si no existe
                    Files.delete(dir); // Opcional: elimina el directorio vacío en el origen
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e) {
            System.err.println("Error en mover la carpeta: ");
            System.err.println("Origen: " + origen);
            System.err.println("Destino: " + destino);
            System.err.println(e.getMessage());
        }
    }
}