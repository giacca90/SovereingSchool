package com.sovereingschool.back_base.Services;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static int[] getVideoResolution(String inputFileName) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("ffprobe",
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=width,height",
                "-of", "csv=p=0", inputFileName);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        int width = 0;
        int height = 0;

        while ((line = reader.readLine()) != null) {
            System.out.println("line: " + line);

            String[] parts = line.split(",");
            width = Integer.parseInt(parts[0]);
            height = Integer.parseInt(parts[1]);
            break;

        }
        process.waitFor();
        return new int[] { width, height };
    }

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
        if (curso.getClases_curso() != null && !curso.getClases_curso().isEmpty()) {
            for (Clase clase : curso.getClases_curso()) {
                Path base = Paths.get(baseUploadDir.toString(), curso.getId_curso().toString(),
                        clase.getId_clase().toString());

                // Verificar que la dirección de la clase no esté vacía y que sea diferente de
                // la base
                if (!clase.getDireccion_clase().isEmpty() && !clase.getDireccion_clase().equals(base.toString())) {

                    // Extraer el directorio y el nombre del archivo de entrada
                    Path inputPath = Paths.get(clase.getDireccion_clase());
                    System.out.println("INPUT: " + inputPath.toString());
                    File baseFile = base.toFile();
                    File inputFile = inputPath.toFile();
                    File destino = new File(baseFile, inputPath.getFileName().toString());
                    System.out.println("DESTINO: " + destino.toString());

                    // Mover el archivo de entrada a la carpeta de destino
                    if (!inputFile.renameTo(destino)) {
                        System.err.println("Error al mover el video a la carpeta de destino");
                        continue; // Cambiar a continue para no romper el bucle
                    }

                    String inputFileName = destino.getName();

                    try {
                        // Obtener la resolución del video
                        int[] resolution = getVideoResolution(destino.toString());
                        int width = resolution[0];
                        int height = resolution[1];
                        System.out.println("Resolución: " + width + "x" + height);

                        // Construcción del comando dependiendo de la resolución
                        List<String> comando = new ArrayList<>();
                        List<String> maps = new ArrayList<>();
                        List<String> audio = new ArrayList<>();
                        List<String> lista = new ArrayList<>();

                        // Construcción de comandos según la resolución
                        if (width >= 1920 && height >= 1080) {
                            comando.add(
                                    "[0:v]split=4[v1][v2][v3][v4]; [v1]copy[v1out]; [v2]scale=w=1280:h=720[v2out]; [v3]scale=w=854:h=480[v3out]; [v4]scale=w=640:h=360[v4out]");
                            maps.addAll(Arrays.asList(
                                    // Resolución 1080p
                                    "-map", "[v1out]", "-c:v:0", "libx264", "-b:v:0", "5M", "-maxrate:v:0", "5M",
                                    "-minrate:v:0", "5M", "-bufsize:v:0", "10M", "-preset", "slow", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_0/",
                                    // Resolución 720p
                                    "-map", "[v2out]", "-c:v:1", "libx264", "-b:v:1", "3M", "-maxrate:v:1", "3M",
                                    "-minrate:v:1", "3M", "-bufsize:v:1", "3M", "-preset", "slow", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_1/",
                                    // Resolución 480p
                                    "-map", "[v3out]", "-c:v:2", "libx264", "-b:v:2", "1M", "-maxrate:v:2", "1M",
                                    "-minrate:v:2", "1M", "-bufsize:v:2", "1M", "-preset", "slow", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_2/",
                                    // Resolución 360p
                                    "-map", "[v4out]", "-c:v:3", "libx264", "-b:v:3", "512k", "-maxrate:v:3", "512k",
                                    "-minrate:v:3", "512k", "-bufsize:v:3", "1M", "-preset", "slow", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_3/"));
                            lista.add("v:0,a:0");
                            lista.add("v:1,a:1");
                            lista.add("v:2,a:2");
                            lista.add("v:3,a:3");
                            audio.addAll(Arrays.asList("-map", "a:0", "-c:a:0", "aac", "-b:a:0", "96k", "-ac", "2"));
                            audio.addAll(Arrays.asList("-map", "a:0", "-c:a:1", "aac", "-b:a:1", "96k", "-ac", "2"));
                            audio.addAll(Arrays.asList("-map", "a:0", "-c:a:2", "aac", "-b:a:2", "96k", "-ac", "2"));
                            audio.addAll(Arrays.asList("-map", "a:0", "-c:a:3", "aac", "-b:a:3", "48k", "-ac", "2"));
                        } else if (width >= 1280 && height >= 720) {
                            comando.add(
                                    "[0:v]split=3[v1][v2][v3]; [v1]copy[v1out]; [v2]scale=w=854:h=480[v2out]; [v3]scale=w=640:h=360[v3out]");
                            maps.addAll(Arrays.asList(
                                    // Resolución 720p
                                    "-map", "[v1out]", "-c:v:0", "libx264", "-b:v:0", "5M", "-maxrate:v:0", "5M",
                                    "-minrate:v:0", "5M", "-bufsize:v:0", "10M", "-preset", "slow", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_0/",
                                    // Resolución 480p
                                    "-map", "[v2out]", "-c:v:1", "libx264", "-b:v:1", "3M", "-maxrate:v:1", "3M",
                                    "-minrate:v:1", "3M", "-bufsize:v:1", "3M", "-preset", "slow", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_1/",
                                    // Resolución 360p
                                    "-map", "[v3out]", "-c:v:2", "libx264", "-b:v:2", "1M", "-maxrate:v:2", "1M",
                                    "-minrate:v:2", "1M", "-bufsize:v:2", "1M", "-preset", "slow", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_2/"));
                            lista.add("v:0,a:0");
                            lista.add("v:1,a:1");
                            lista.add("v:2,a:2");
                            audio.addAll(Arrays.asList("-map", "a:0", "-c:a:0", "aac", "-b:a:0", "96k", "-ac", "2"));
                            audio.addAll(Arrays.asList("-map", "a:0", "-c:a:1", "aac", "-b:a:1", "96k", "-ac", "2"));
                            audio.addAll(Arrays.asList("-map", "a:0", "-c:a:2", "aac", "-b:a:2", "48k", "-ac", "2"));
                        } else if (width >= 854 && height >= 480) {
                            comando.add("[0:v]split=2[v1][v2]; [v1]copy[v1out]; [v2]scale=w=640:h=360[v2out]");
                            maps.addAll(Arrays.asList(
                                    // Resolución 480p
                                    "-map", "[v1out]", "-c:v:0", "libx264", "-b:v:0", "3M", "-maxrate:v:0", "3M",
                                    "-minrate:v:0", "3M", "-bufsize:v:0", "6M", "-preset", "slow", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_0/",
                                    // Resolución 360p
                                    "-map", "[v2out]", "-c:v:1", "libx264", "-b:v:1", "1M", "-maxrate:v:1", "1M",
                                    "-minrate:v:1", "1M", "-bufsize:v:1", "2M", "-preset", "slow", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_1/"));
                            lista.add("v:0,a:0");
                            lista.add("v:1,a:1");
                            audio.addAll(Arrays.asList("-map", "a:0", "-c:a:0", "aac", "-b:a:0", "96k", "-ac", "2"));
                            audio.addAll(Arrays.asList("-map", "a:0", "-c:a:1", "aac", "-b:a:1", "48k", "-ac", "2"));
                        } else {
                            comando.add("[0:v]copy[v1out]");
                            maps.addAll(Arrays.asList(
                                    // Resolución 420p
                                    "-map", "[v1out]", "-c:v:0", "libx264", "-b:v:0", "512k", "-maxrate:v:0", "512k",
                                    "-minrate:v:0", "512k", "-bufsize:v:0", "1M", "-preset", "slow", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_0/"));
                            lista.add("v:0,a:0");
                            audio.addAll(Arrays.asList("-map", "a:0", "-c:a:3", "aac", "-b:a:3", "48k", "-ac", "2"));
                        }

                        // Construcción del comando FFmpeg
                        List<String> ffmpegCommand = new ArrayList<>();
                        ffmpegCommand.add("ffmpeg");
                        ffmpegCommand.add("-i");
                        ffmpegCommand.add(inputFileName);
                        ffmpegCommand.add("-filter_complex");
                        ffmpegCommand.add(comando.get(0)); // Se pasa como string

                        // Añadir los mapeos de video y audio al comando
                        ffmpegCommand.addAll(maps);
                        ffmpegCommand.addAll(audio);

                        // Opciones de salida de HLS
                        ffmpegCommand.addAll(Arrays.asList(
                                "-f", "hls",
                                "-hls_time", "5",
                                "-hls_playlist_type", "vod",
                                "-hls_flags", "independent_segments",
                                "-hls_segment_type", "mpegts",
                                "-master_pl_name", "master.m3u8",
                                "-var_stream_map", String.join(" ", lista),
                                "stream_%v.m3u8"));

                        // Ejecutar el comando FFmpeg
                        System.out.println("COMANDO FINAL: " + String.join(" ", ffmpegCommand));
                        ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand);

                        // Establecer el directorio de trabajo
                        processBuilder.directory(baseFile);
                        processBuilder.redirectErrorStream(true);

                        Process process = processBuilder.start();

                        // Leer la salida del proceso
                        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                System.out.println(line);
                            }
                        }

                        int exitCode = process.waitFor();
                        if (exitCode != 0) {
                            throw new IOException("El proceso de FFmpeg falló con el código de salida " + exitCode);
                        }

                        clase.setDireccion_clase(base.toString() + "/master.m3u8");
                        this.claseRepo.save(clase);

                    } catch (IOException e) {
                        System.err.println("Error en la entrada/salida: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Error en convertir el video: " + e.getMessage());
                    }
                }
            }
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

    // TODO: Comprobar
    public void startLiveStreamingFromStream(String userId, InputStream inputStream) throws IOException {
        Path outputDir = baseUploadDir.resolve(userId);
        System.out.println("Salida: " + outputDir);

        // Crear el directorio de salida si no existe
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
            System.out.println("Carpeta creada: " + outputDir);
        }

        // Comando FFmpeg para procesar el streaming
        List<String> ffmpegCommand = List.of(
                "ffmpeg",
                "-loglevel", "info",
                "-re",
                "-i", "pipe:0",
                // Parámetros HLS
                "-f", "hls",
                "-hls_time", "5",
                "-hls_playlist_type", "event",
                "-hls_flags", "delete_segments+independent_segments",
                "-hls_segment_type", "mpegts",
                // Crea filtros
                "-filter_complex",
                "[0:v]split=4[v1][v2][v3][v4];" +
                        "[v1]copy[v1out];" +
                        "[v2]scale=w=1280:h=720[v2out];" +
                        "[v3]scale=w=854:h=480[v3out];" +
                        "[v4]scale=w=640:h=360[v4out]",
                // Mapas de video y audio para múltiples resoluciones
                // Resolución 1080p
                "-map", "[v1out]", "-c:v:0", "libx264", "-b:v:0", "5M", "-maxrate:v:0", "5M", "-minrate:v:0", "5M",
                "-bufsize:v:0", "10M", "-preset", "veryfast", "-g", "48", "-sc_threshold", "0", "-keyint_min", "48",
                "-hls_segment_filename", outputDir + "/stream_%v/data%03d.ts",
                "-hls_base_url", "stream_0/",
                // Resolución 720p
                "-map", "[v2out]", "-c:v:1", "libx264", "-b:v:1", "3M", "-maxrate:v:1", "3M", "-minrate:v:1", "3M",
                "-bufsize:v:1", "6M", "-preset", "veryfast", "-g", "48", "-sc_threshold", "0", "-keyint_min", "48",
                "-hls_segment_filename", outputDir + "/stream_%v/data%03d.ts",
                "-hls_base_url", "stream_1/",
                // Resolución 480p
                "-map", "[v3out]", "-c:v:2", "libx264", "-b:v:2", "1M", "-maxrate:v:2", "1M", "-minrate:v:2", "1M",
                "-bufsize:v:2", "2M", "-preset", "veryfast", "-g", "48", "-sc_threshold", "0", "-keyint_min", "48",
                "-hls_segment_filename", outputDir + "/stream_%v/data%03d.ts",
                "-hls_base_url", "stream_2/",
                // Resolución 360p
                "-map", "[v4out]", "-c:v:3", "libx264", "-b:v:3", "512k", "-maxrate:v:3", "512k", "-minrate:v:3",
                "512k",
                "-bufsize:v:3", "1M", "-preset", "veryfast", "-g", "48", "-sc_threshold", "0", "-keyint_min", "48",
                "-hls_segment_filename", outputDir + "/stream_%v/data%03d.ts",
                "-hls_base_url", "stream_3/",
                // Mapeo de audio
                "-map", "a:0", "-c:a:0", "aac", "-b:a:0", "128k", "-ac", "2",
                "-map", "a:0", "-c:a:1", "aac", "-b:a:1", "128k", "-ac", "2",
                "-map", "a:0", "-c:a:2", "aac", "-b:a:2", "96k", "-ac", "2",
                "-map", "a:0", "-c:a:3", "aac", "-b:a:3", "64k", "-ac", "2",

                // Comando locales
                "-master_pl_name", "master.m3u8",
                "-var_stream_map", "v:0,a:0 v:1,a:1 v:2,a:2 v:3,a:3",
                outputDir + "/stream_%v.m3u8",
                // Guardar en MP4 en la resolución original
                "-map", "0:v", "-map", "0:a", "-c:v", "copy", "-c:a", "aac", outputDir + "/original.mp4");

        ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedOutputStream ffmpegInput = new BufferedOutputStream(process.getOutputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            // Hilo para leer y mostrar los logs de FFmpeg
            Thread logReader = new Thread(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("FFmpeg: " + line);
                    }
                } catch (IOException e) {
                    System.err.println("Error leyendo salida de FFmpeg: " + e.getMessage());
                }
            });
            logReader.start();

            // Escribir datos del InputStream en el proceso FFmpeg
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                ffmpegInput.write(buffer, 0, bytesRead);
                ffmpegInput.flush();
            }

            ffmpegInput.close(); // Cerrar el flujo hacia FFmpeg
            logReader.join(); // Esperar a que se terminen de leer los logs

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("FFmpeg terminó con código de salida " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error en FFmpeg: " + e.getMessage());
            throw new IOException(e);
        }
    }

    // TODO: Comprobar
    public void startLiveStreamingFromRTMP(String rtmpUrl) throws IOException {
        // Define la carpeta de salida donde se guardarán los archivos .ts y .m3u8
        Path outputDir = Paths.get("output/stream");

        List<String> ffmpegCommand = new ArrayList<>();
        ffmpegCommand.add("ffmpeg");
        ffmpegCommand.add("-i");
        ffmpegCommand.add(rtmpUrl); // URL del flujo RTMP entrante
        ffmpegCommand.add("-c:v");
        ffmpegCommand.add("libx264");
        ffmpegCommand.add("-preset");
        ffmpegCommand.add("fast");
        ffmpegCommand.add("-f");
        ffmpegCommand.add("hls");
        ffmpegCommand.add("-hls_time");
        ffmpegCommand.add("5");
        ffmpegCommand.add("-hls_playlist_type");
        ffmpegCommand.add("event");
        ffmpegCommand.add("-hls_flags");
        ffmpegCommand.add("delete_segments");
        ffmpegCommand.add("-hls_segment_filename");
        ffmpegCommand.add(outputDir + "/stream_%03d.ts");
        ffmpegCommand.add(outputDir + "/index.m3u8");

        // Ejecutar el comando FFmpeg
        ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("El proceso de FFmpeg falló con el código de salida " + exitCode);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}