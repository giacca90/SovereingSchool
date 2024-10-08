package com.sovereingschool.back_base.Services;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import reactor.core.publisher.Mono;

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
    private final String backStreamURL = "http://localhost:8090";
    private final String backChatURL = "http://localhost:8070";

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
    @Async
    public void convertVideos(Curso curso) {
        if (curso.getClases_curso() != null && curso.getClases_curso().size() > 0) {
            for (Clase clase : curso.getClases_curso()) {
                Path base = Paths.get(baseUploadDir.toString(), curso.getId_curso().toString(),
                        clase.getId_clase().toString());
                if (clase.getDireccion_clase().length() > 0 && !clase.getDireccion_clase().equals(base)) {

                    // Extrae el directorio y el nombre del archivo de entrada
                    Path inputPath = Paths.get(clase.getDireccion_clase());
                    System.out.println("INPUT: " + inputPath.toString());
                    File baseFile = new File(base.toString());
                    File inputFile = new File(inputPath.toString());
                    File destino = new File(baseFile, inputPath.getFileName().toString());
                    System.out.println("DESTINO: " + destino.toString());
                    if (!inputFile.renameTo(destino)) {
                        System.err.println("Error en mover el video a la carpeta de destino");
                        break;
                    }
                    String inputFileName = destino.getName().toString();

                    // Construye el comando FFmpeg
                    ProcessBuilder processBuilder = new ProcessBuilder(
                            "ffmpeg",
                            "-i", inputFileName,
                            "-filter_complex",
                            "[0:v]split=3[v1][v2][v3]; [v1]copy[v1out]; [v2]scale=w=1280:h=720[v2out]; [v3]scale=w=640:h=360[v3out]",
                            "-map", "[v1out]", "-c:v:0", "libx264", "-b:v:0", "5M", "-maxrate:v:0", "5M",
                            "-minrate:v:0",
                            "5M",
                            "-bufsize:v:0", "10M", "-preset", "slow", "-g", "48", "-sc_threshold", "0", "-keyint_min",
                            "48",
                            "-map", "[v2out]", "-c:v:1", "libx264", "-b:v:1", "3M", "-maxrate:v:1", "3M",
                            "-minrate:v:1",
                            "3M",
                            "-bufsize:v:1", "3M", "-preset", "slow", "-g", "48", "-sc_threshold", "0", "-keyint_min",
                            "48",
                            "-map", "[v3out]", "-c:v:2", "libx264", "-b:v:2", "1M", "-maxrate:v:2", "1M",
                            "-minrate:v:2",
                            "1M",
                            "-bufsize:v:2", "1M", "-preset", "slow", "-g", "48", "-sc_threshold", "0", "-keyint_min",
                            "48",
                            "-map", "a:0", "-c:a:0", "aac", "-b:a:0", "96k", "-ac", "2",
                            "-map", "a:0", "-c:a:1", "aac", "-b:a:1", "96k", "-ac", "2",
                            "-map", "a:0", "-c:a:2", "aac", "-b:a:2", "48k", "-ac", "2",
                            "-f", "hls",
                            "-hls_time", "5",
                            "-hls_playlist_type", "vod",
                            "-hls_flags", "independent_segments",
                            "-hls_segment_type", "mpegts",
                            "-hls_segment_filename", "stream_%v/data%02d.ts",
                            "-hls_base_url", "stream_%v/",
                            "-master_pl_name", "master.m3u8",
                            "-var_stream_map", "v:0,a:0 v:1,a:1 v:2,a:2",
                            "stream_%v.m3u8");

                    // Establece el directorio de trabajo al directorio que contiene el archivo de
                    // entrada
                    processBuilder.directory(new java.io.File(base.toString()));
                    processBuilder.redirectErrorStream(true);

                    // Inicia el proceso
                    try {
                        Process process = processBuilder.start();

                        // Lee la salida del proceso para depuración
                        try (var reader = new java.io.BufferedReader(
                                new java.io.InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                System.out.println(line);
                            }
                        }

                        // Espera a que el proceso termine
                        int exitCode = process.waitFor();
                        if (exitCode != 0) {
                            throw new IOException("FFmpeg process failed with exit code " + exitCode);
                        } else {
                            List<Path> m3u8Files = new ArrayList<>();
                            DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(base.toString()),
                                    "*.m3u8");
                            for (Path entry : stream) {
                                if (!entry.endsWith("master.m3u8")) {
                                    m3u8Files.add(entry);
                                }
                            }
                            for (Path m3u8File : m3u8Files) {
                                String fileName = m3u8File.toString().substring(
                                        m3u8File.toString().lastIndexOf("/") + 1,
                                        m3u8File.toString().lastIndexOf("."));
                                List<String> lines = Files.readAllLines(m3u8File, StandardCharsets.UTF_8);
                                List<String> modifiedLines = new ArrayList<>();

                                for (String line : lines) {
                                    // Modifica la línea según tus necesidades
                                    if (line.startsWith("stream_%v/")) {
                                        line = line.replace("stream_%v/", fileName + "/");
                                    }
                                    modifiedLines.add(line);
                                }

                                Files.write(m3u8File, modifiedLines, StandardCharsets.UTF_8);
                            }
                            stream.close();
                            clase.setDireccion_clase(base.toString() + "/master.m3u8");
                            this.claseRepo.save(clase);
                        }
                    } catch (Exception e) {
                        System.err.println("Error en convertir el video: " + e.getCause());
                    }
                }
            }
        }
    }

    @Override
    public String subeVideo(MultipartFile file) {
        try {
            // Define el path para guardar el archivo subido
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            Path filePath = baseUploadDir
                    .resolve(UUID.randomUUID().toString() + "_"
                            + originalFileName.replaceAll("[^a-zA-Z0-9\\s.]", "")
                                    .replaceAll("\\s+", "_"));
            // Guarda el archivo en el servidor
            Files.write(filePath, file.getBytes());
            return filePath.normalize().toString();

        } catch (Exception e) {
            System.err.println("Error en subir el video: " + e.getMessage());
            return null;
        }
    }
}