package com.sovereingschool.back_streaming.Services;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sovereingschool.back_streaming.Models.Clase;
import com.sovereingschool.back_streaming.Models.Curso;
import com.sovereingschool.back_streaming.Repositories.ClaseRepository;

@Service
@Transactional
public class StreamingService {
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

    private final Map<String, Process> ffmpegProcesses = new ConcurrentHashMap<>();
    private final Map<String, Process> previewProcesses = new ConcurrentHashMap<>();

    @Autowired
    private ClaseRepository claseRepo;

    private final String uploadDir = "/home/matt/Escritorio/Proyectos/SovereingSchool/Videos";

    private final Path baseUploadDir = Paths.get(uploadDir); // Directorio base para subir videos

    /**
     * Función para convertir los videos de un curso
     * 
     * @param curso
     */
    @Async
    public void convertVideos(Curso curso) {
        if (curso.getClases_curso() != null && !curso.getClases_curso().isEmpty()) {
            for (Clase clase : curso.getClases_curso()) {
                Path base = Paths.get(baseUploadDir.toString(), curso.getId_curso().toString(),
                        clase.getId_clase().toString());

                // Verificar que la dirección de la clase no esté vacía y que sea diferente de
                // la base
                if (!clase.getDireccion_clase().isEmpty() && !clase.getDireccion_clase().equals(base.toString())
                        && !clase.getDireccion_clase().endsWith(".m3u8")) {

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
                        continue;
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
                                    "-minrate:v:0", "5M", "-bufsize:v:0", "10M", "-preset", "fast", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_0/",
                                    // Resolución 720p
                                    "-map", "[v2out]", "-c:v:1", "libx264", "-b:v:1", "3M", "-maxrate:v:1", "3M",
                                    "-minrate:v:1", "3M", "-bufsize:v:1", "3M", "-preset", "fast", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_1/",
                                    // Resolución 480p
                                    "-map", "[v3out]", "-c:v:2", "libx264", "-b:v:2", "1M", "-maxrate:v:2", "1M",
                                    "-minrate:v:2", "1M", "-bufsize:v:2", "1M", "-preset", "fast", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_2/",
                                    // Resolución 360p
                                    "-map", "[v4out]", "-c:v:3", "libx264", "-b:v:3", "512k", "-maxrate:v:3", "512k",
                                    "-minrate:v:3", "512k", "-bufsize:v:3", "1M", "-preset", "fast", "-g", "48",
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
                                    "-minrate:v:0", "5M", "-bufsize:v:0", "10M", "-preset", "fast", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_0/",
                                    // Resolución 480p
                                    "-map", "[v2out]", "-c:v:1", "libx264", "-b:v:1", "3M", "-maxrate:v:1", "3M",
                                    "-minrate:v:1", "3M", "-bufsize:v:1", "3M", "-preset", "fast", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_1/",
                                    // Resolución 360p
                                    "-map", "[v3out]", "-c:v:2", "libx264", "-b:v:2", "1M", "-maxrate:v:2", "1M",
                                    "-minrate:v:2", "1M", "-bufsize:v:2", "1M", "-preset", "fast", "-g", "48",
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
                                    "-minrate:v:0", "3M", "-bufsize:v:0", "6M", "-preset", "fast", "-g", "48",
                                    "-sc_threshold", "0", "-keyint_min", "48", "-hls_segment_filename",
                                    "stream_%v/data%02d.ts",
                                    "-hls_base_url", "stream_0/",
                                    // Resolución 360p
                                    "-map", "[v2out]", "-c:v:1", "libx264", "-b:v:1", "1M", "-maxrate:v:1", "1M",
                                    "-minrate:v:1", "1M", "-bufsize:v:1", "2M", "-preset", "fast", "-g", "48",
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
                                    "-minrate:v:0", "512k", "-bufsize:v:0", "1M", "-preset", "fast", "-g", "48",
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

    /**
     * Función para iniciar la transmisión en vivo
     * 
     * @param userId
     * @param inputStream
     * @throws Exception
     */
    public void startLiveStreamingFromStream(String userId, Object inputStream) throws Exception {
        Path outputDir = baseUploadDir.resolve(userId);
        System.out.println("Salida: " + outputDir);

        // Crear el directorio de salida si no existe
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
            System.out.println("Carpeta creada: " + outputDir);
        }

        // Determinar el origen: PipedInputStream o RTMP URL
        String inputSpecifier;
        if (inputStream instanceof PipedInputStream) {
            inputSpecifier = "pipe:0"; // Entrada desde el pipe
        } else if (inputStream instanceof String) {
            inputSpecifier = "rtmp://localhost:8060/live/" + userId;// Entrada desde una URL RTMP
        } else {
            throw new IllegalArgumentException("Fuente de entrada no soportada");
        }

        // Comando FFmpeg para procesar el streaming
        List<String> ffmpegCommand = List.of(
                "ffmpeg",
                "-loglevel", "warning",
                "-re",
                "-i", inputSpecifier,
                // Parámetros HLS
                "-f", "hls",
                "-hls_time", "5",
                "-hls_playlist_type", "event",
                "-hls_flags", "delete_segments+independent_segments+append_list", // Asegura continuidad
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
        // Guardar el proceso en el mapa
        ffmpegProcesses.put(userId.substring(userId.lastIndexOf("_") + 1), process);

        try (BufferedOutputStream ffmpegInput = new BufferedOutputStream(process.getOutputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            // Hilo para leer los logs de FFmpeg
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

            // Escribir datos en el proceso (solo WebCam)
            if (inputStream instanceof PipedInputStream) {
                try (InputStream inStream = (InputStream) inputStream) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        ffmpegInput.write(buffer, 0, bytesRead);
                        ffmpegInput.flush();
                    }
                }
            }

            logReader.join(); // Esperar a que se terminen de leer los logs
        } catch (IOException | InterruptedException e) {
            System.err.println("Error en FFmpeg: " + e.getMessage());
            process.destroy(); // Forzar cierre del proceso en caso de error
            throw e;
        }
    }

    public void stopFFmpegProcessForUser(String userId) throws IOException {
        String sessionId = userId.substring(userId.lastIndexOf('_') + 1);
        Process process = ffmpegProcesses.remove(sessionId);
        if (process != null && process.isAlive()) {
            try {
                OutputStream os = process.getOutputStream();
                os.write('q'); // Señal de terminación
                os.flush();
                os.close();

                boolean finished = process.waitFor(1, TimeUnit.SECONDS); // Esperar 5 segundos
                if (finished) {
                    // El proceso terminó correctamente
                    int exitCode = process.exitValue();
                    if (exitCode == 0) {
                        System.out.println("Proceso FFmpeg preview detenido correctamente para el usuario " + userId);
                    } else {
                        System.out.println("FFmpeg preview terminó con un error. Código de salida: " + exitCode);
                    }
                } else {
                    // Si no terminó en 1 segundo, forzar la terminación
                    System.err.println(
                            "El proceso FFmpeg preview no respondió en el tiempo esperado. Terminando de forma forzada...");
                    process.destroy(); // Intentar una terminación limpia
                    if (process.isAlive()) {
                        process.destroyForcibly(); // Forzar si sigue vivo
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("El proceso fue interrumpido: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        } else {
            System.err.println("No se encontró un proceso FFmpeg para el usuario " + userId);
        }

        Process preProcess = previewProcesses.remove(sessionId);
        if (preProcess != null && preProcess.isAlive()) {
            try {
                // Enviar una señal de terminación controlada
                OutputStream os = preProcess.getOutputStream();
                os.write('q'); // Enviar la letra 'q'
                os.flush(); // Asegurarse de que se envíe
                os.close();
                // Esperar a que el proceso termine de forma controlada
                // Esperar un segundo para que termine de manera controlada
                boolean finished = preProcess.waitFor(1, TimeUnit.SECONDS);

                if (finished) {
                    // El proceso terminó correctamente
                    int exitCode = preProcess.exitValue();
                    if (exitCode == 0) {
                        System.out.println("Proceso FFmpeg preview detenido correctamente para el usuario " + userId);
                    } else {
                        System.out.println("FFmpeg preview terminó con un error. Código de salida: " + exitCode);
                    }
                } else {
                    // Si no terminó en 1 segundo, forzar la terminación
                    System.err.println(
                            "El proceso FFmpeg preview no respondió en el tiempo esperado. Terminando de forma forzada...");
                    preProcess.destroy(); // Intentar una terminación limpia
                    if (preProcess.isAlive()) {
                        preProcess.destroyForcibly(); // Forzar si sigue vivo
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("El proceso fue interrumpido: " + e.getMessage());
                Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
            }

            // Elimina la carpeta de la preview
            Path previewDir = baseUploadDir.resolve("previews").resolve(userId);
            if (!Files.exists(previewDir) || !Files.isDirectory(previewDir)) {
                // Si la carpeta no existe, buscar la carpeta con el mismo nombre
                String temp = userId;
                userId = Files.walk(previewDir.getParent())
                        .sorted(Comparator.reverseOrder())
                        .filter(path -> path.getFileName().toString().contains(temp))
                        .findFirst().get().toString();
                previewDir = baseUploadDir.resolve("previews").resolve(userId);
            }
            try {
                Files.walk(previewDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                System.err.println("Error al eliminar la carpeta de la previsualización: " + e.getMessage());
            }
            Path m3u8 = baseUploadDir.resolve("previews").resolve(userId + ".m3u8");
            if (Files.exists(m3u8)) {
                Files.delete(m3u8);
            }
        } else {
            System.err.println("No se encontró un proceso de previsualizació  para el usuario " + userId);
        }
    }

    /**
     * Función para iniciar la previsualización del flujo de RTMP
     * 
     * @param rtmpUrl
     * @throws IOException
     * @throws InterruptedException
     */
    @Async
    public void startPreview(String rtmpUrl) throws IOException, InterruptedException {
        String previewId = rtmpUrl.substring(rtmpUrl.lastIndexOf("/") + 1);
        Path previewDir = baseUploadDir.resolve("previews");
        // Crear el directorio de salida si no existe
        if (!Files.exists(previewDir)) {
            Files.createDirectories(previewDir);
            System.out.println("Carpeta creada: " + previewDir);
        }

        Path outputDir = previewDir.resolve(previewId);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
            System.out.println("Carpeta creada: " + outputDir);
        }

        // preparar comando FFmpeg preview
        List<String> ffmpegCommand = List.of(
                "ffmpeg",
                "-re",
                "-i", rtmpUrl,
                "-preset", "veryfast",
                "-tune", "zerolatency",
                "-fflags", "nobuffer",
                "-loglevel", "warning",
                "-f", "hls",
                "-hls_time", "0.5",
                "-hls_list_size", "2",
                "-hls_flags", "delete_segments+independent_segments+program_date_time",
                "-hls_segment_type", "mpegts",
                "-hls_segment_filename", outputDir + "/%03d.ts",
                "-hls_base_url", previewId + "/",
                "-g", "10",
                previewDir + "/" + previewId + ".m3u8");

        ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        this.previewProcesses.put(previewId.substring(previewId.lastIndexOf("_") + 1), process);

        // Capturar logs del proceso FFmpeg
        Thread logReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("FFmpeg preview: " + line); // Mostrar logs en la consola
                }
            } catch (IOException e) {
                System.err.println("Error leyendo salida de FFmpeg preview: " + e.getMessage());
            }
        });
        logReader.start();

        logReader.join(); // Esperar a que se terminen de leer los logs
    }

    public Path getPreview(String id_preview) {
        Path previewDir = baseUploadDir.resolve("previews");
        Path m3u8 = previewDir.resolve(id_preview + ".m3u8");
        while (previewProcesses.containsKey(id_preview.substring(id_preview.lastIndexOf("_") + 1))) {
            // Espera a que se genere el preview
            if (Files.exists(m3u8)) {
                return m3u8;
            }
        }
        return null;
    }
}
