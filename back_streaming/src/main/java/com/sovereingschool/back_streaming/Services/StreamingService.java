package com.sovereingschool.back_streaming.Services;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Autowired
    private ClaseRepository claseRepo;

    private final String uploadDir = "/home/matt/Escritorio/Proyectos/SovereingSchool/Videos";

    private final Path baseUploadDir = Paths.get(uploadDir); // Directorio base para subir videos

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

    // TODO: Comprobar
    public void startLiveStreamingFromStream(String userId, Object inputStream) throws IOException {
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
            inputSpecifier = (String) inputStream; // Entrada desde una URL RTMP
        } else {
            throw new IllegalArgumentException("Fuente de entrada no soportada");
        }

        // Comando FFmpeg para procesar el streaming
        List<String> ffmpegCommand = List.of(
                "ffmpeg",
                "-loglevel", "info",
                "-re",
                "-i", inputSpecifier,
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
            if (inputStream instanceof PipedInputStream) {
                InputStream inStream = (InputStream) inputStream; // Cast explícito
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inStream.read(buffer)) != -1) {
                    ffmpegInput.write(buffer, 0, bytesRead);
                    ffmpegInput.flush();
                }
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
}
