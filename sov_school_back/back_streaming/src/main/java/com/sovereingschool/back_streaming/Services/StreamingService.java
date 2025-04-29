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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sovereingschool.back_common.Models.Clase;
import com.sovereingschool.back_common.Models.Curso;
import com.sovereingschool.back_common.Repositories.ClaseRepository;

@Service
@Transactional
public class StreamingService {
    private final Map<String, Process> ffmpegProcesses = new ConcurrentHashMap<>();
    private final Map<String, Process> previewProcesses = new ConcurrentHashMap<>();

    @Autowired
    private ClaseRepository claseRepo;

    @Value("${variable.RTMP}")
    private String RTMP;

    private final String uploadDir = "/home/matt/Escritorio/Proyectos/SovereingSchool/Videos";

    private final Path baseUploadDir = Paths.get(uploadDir); // Directorio base para subir videos

    /**
     * Función para convertir los videos de un curso
     * 
     * @param curso
     * @throws IOException
     * @throws InterruptedException
     */
    @Async
    public void convertVideos(Curso curso) throws IOException, InterruptedException {
        if (curso.getClases_curso() != null && !curso.getClases_curso().isEmpty()) {
            for (Clase clase : curso.getClases_curso()) {
                Path base = Paths.get(baseUploadDir.toString(), curso.getId_curso().toString(),
                        clase.getId_clase().toString());

                // Verificar que la dirección de la clase no esté vacía y que sea diferente dela
                // base
                if (!clase.getDireccion_clase().isEmpty() && !clase.getDireccion_clase().equals(base.toString())
                        && !clase.getDireccion_clase().endsWith(".m3u8")) {

                    // Extraer el directorio y el nombre del archivo de entrada
                    Path inputPath = Paths.get(clase.getDireccion_clase());
                    File baseFile = base.toFile();
                    File inputFile = inputPath.toFile();
                    File destino = new File(baseFile, inputPath.getFileName().toString());

                    // Mover el archivo de entrada a la carpeta de destino
                    if (!inputFile.renameTo(destino)) {
                        System.err.println("Error al mover el video a la carpeta de destino");
                        continue;
                    }
                    List<String> ffmpegCommand = null;
                    try {
                        ffmpegCommand = this.creaComandoFFmpeg(destino.getAbsolutePath(), false, null, null);
                    } catch (IOException | InterruptedException e) {
                        System.err.println("Error al generar el comando FFmpeg: " + e.getMessage());
                    }
                    if (ffmpegCommand != null) {
                        // Ejecutar el comando FFmpeg
                        ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand);

                        // Establecer el directorio de trabajo
                        processBuilder.directory(baseFile);
                        processBuilder.redirectErrorStream(true);

                        Process process = processBuilder.start();

                        // Leer la salida del proceso
                        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                System.err.println("FFmpeg: " + line);
                            }
                        }

                        int exitCode = process.waitFor();
                        if (exitCode != 0) {
                            throw new IOException("El proceso de FFmpeg falló con el código de salida " + exitCode);
                        }

                        clase.setDireccion_clase(base.toString() + "/master.m3u8");
                        this.claseRepo.save(clase);
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
    public void startLiveStreamingFromStream(String[] streamIdAndSettings, Object inputStream,
            PipedInputStream ffmpegInputStream)
            throws Exception {
        String userId = streamIdAndSettings[0];
        Path outputDir = baseUploadDir.resolve(userId);

        // Crear el directorio de salida si no existe
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // Determinar el origen: PipedInputStream o RTMP URL
        String inputSpecifier;
        List<String> ffmpegCommand = null;
        if (inputStream instanceof PipedInputStream) {
            inputSpecifier = "pipe:0"; // Entrada desde el pipe
            ffmpegCommand = this.creaComandoFFmpeg(inputSpecifier, true, (InputStream) inputStream,
                    streamIdAndSettings);
        } else if (inputStream == null) {
            inputSpecifier = "pipe:0"; // Entrada desde el pipe
            ffmpegCommand = this.creaComandoFFmpeg(inputSpecifier, true, ffmpegInputStream, streamIdAndSettings);
        } else if (inputStream instanceof String) {
            inputSpecifier = RTMP + "/live/" + userId;// Entrada desde una URL RTMP
            ffmpegCommand = this.creaComandoFFmpeg(inputSpecifier, true, null, streamIdAndSettings);
        } else {
            throw new IllegalArgumentException("Fuente de entrada no soportada");
        }

        // Comando FFmpeg para procesar el streaming
        System.out.println("Comando FFmpeg: " + String.join(" ", ffmpegCommand));
        ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand);
        processBuilder.redirectErrorStream(true);
        processBuilder.directory(outputDir.toFile());
        Process process = processBuilder.start();
        // Guardar el proceso en el mapa
        ffmpegProcesses.put(userId.substring(userId.lastIndexOf("_") + 1), process);

        BufferedOutputStream ffmpegInput = new BufferedOutputStream(process.getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // Hilo para leer los logs de FFmpeg
        Thread logReader = new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("FFmpeg: " + line);
                }
            } catch (IOException e) {
                System.err.println("Error leyendo salida de FFmpeg: " + e.getMessage());
            }
        });
        logReader.start();

        // Escribir datos en el proceso (solo WebCam)
        if (inputStream instanceof PipedInputStream || inputStream == null && ffmpegInputStream != null) {
            InputStream inStream = (InputStream) ffmpegInputStream;
            byte[] buffer = new byte[1024 * 1024];
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                ffmpegInput.write(buffer, 0, bytesRead);
                ffmpegInput.flush();
            }
        }
        logReader.join(); // Esperar a que se terminen de leer los logs
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
                    } else {
                        System.err.println("FFmpeg preview terminó con un error. Código de salida: " + exitCode);
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
                    } else {
                        System.err.println("FFmpeg preview terminó con un error. Código de salida: " + exitCode);
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
            System.err.println("No se encontró un proceso de previsualización  para el usuario " + userId);
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
        }

        Path outputDir = previewDir.resolve(previewId);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
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
                    System.err.println("FFmpeg preview: " + line); // Mostrar logs en la consola
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

    /**
     * Función para generar el comando ffmpeg.
     * El comando debe ser ejecutado en la carpeta de salida.
     * 
     * @param inputFilePath String: dirección del video original
     * @param live          Boolean: bandera para eventos en vivo
     * @return List<String>: el comando generado
     * @throws IOException
     * @throws InterruptedException
     */
    private List<String> creaComandoFFmpeg(String inputFilePath, Boolean live, InputStream inputStream,
            String[] streamIdAndSettings)
            throws IOException, InterruptedException {
        String hls_playlist_type = live ? "event" : "vod";
        String hls_flags = live ? "independent_segments+append_list+program_date_time" : "independent_segments";
        String preset = live ? "veryfast" : "fast";
        String width = null;
        String height = null;
        String fps = null;
        if (streamIdAndSettings != null) {
            width = streamIdAndSettings[1];
            height = streamIdAndSettings[2];
            fps = streamIdAndSettings[3];
        }
        // Obtener la resolución del video
        if (width == null || height == null || fps == null) {

            ProcessBuilder processBuilder = new ProcessBuilder("ffprobe",
                    "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries", "stream=width,height,r_frame_rate",
                    "-of", "csv=p=0", inputFilePath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            Thread ffprobeThread = null;

            // Escribe datos en el proceso (solo webcam)
            if (inputFilePath.contains("pipe:0")) {
                ffprobeThread = new Thread(() -> {
                    BufferedOutputStream ffprobeInput = new BufferedOutputStream(process.getOutputStream());
                    try {
                        byte[] buffer = new byte[1024 * 1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            ffprobeInput.write(buffer, 0, bytesRead);
                            ffprobeInput.flush();
                        }
                        ffprobeInput.close();
                    } catch (IOException e) {
                        System.err.println("Error en escribir datos a ffprobe: " + e.getMessage());
                        try {
                            ffprobeInput.close();
                        } catch (IOException e1) {
                            System.err.println("Error en cerrar flujo de escritura: " + e1.getMessage());
                        }
                    }
                });
                ffprobeThread.start();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                width = parts[0];
                height = parts[1];

                // Para calcular los fps, "r_frame_rate" devuelve un formato como"30000/1001"
                String[] frameRateParts = parts[2].split("/");
                if (frameRateParts.length == 2) {
                    // Redondear el fps a entero
                    fps = String.valueOf((int) Math.round(Double.parseDouble(frameRateParts[0]) /
                            Double.parseDouble(frameRateParts[1])));
                }
                break;
            }

            if (ffprobeThread != null) {
                ffprobeThread.join();
            }
            process.waitFor();
            if (width == "0" || height == "0" || fps == "0") {
                System.err.println("La resolución es 0");
                return null;
            }
        }

        // Calcular las partes necesarias según la resolución
        List<String> resolutionPairs = new ArrayList<>();
        resolutionPairs.add(width + "," + height + "," + fps);
        int tempWidth = Integer.parseInt(width);
        int tempHeight = Integer.parseInt(height);
        while ((tempWidth - tempWidth / 3) * (tempHeight - tempHeight / 3) >= 320 * 180) {
            tempWidth = tempWidth - tempWidth / 3;
            tempHeight = tempHeight - tempHeight / 3;

            // Asegurarse de que las dimensiones sean pares
            if (tempWidth % 2 != 0) {
                tempWidth--;
            }
            if (tempHeight % 2 != 0) {
                tempHeight--;
            }

            resolutionPairs.add(tempWidth + "," + tempHeight + "," + fps);
        }

        if (Integer.parseInt(fps) > 90) {
            int tempWidth2 = Integer.parseInt(width);
            int tempHeight2 = Integer.parseInt(height);
            resolutionPairs.add(tempWidth2 + "," + tempHeight2 + "," + "90");
            while ((tempWidth2 - tempWidth2 / 3) * (tempHeight2 - tempHeight2 / 3) >= 320 * 180) {
                tempWidth2 = tempWidth2 - tempWidth2 / 3;
                tempHeight2 = tempHeight2 - tempHeight2 / 3;
                // Asegurarse de que las dimensiones sean pares
                if (tempWidth2 % 2 != 0) {
                    tempWidth2--;
                }
                if (tempHeight2 % 2 != 0) {
                    tempHeight2--;
                }
                resolutionPairs.add(tempWidth2 + "," + tempHeight2 + "," + "90");
            }

        }

        if (Integer.parseInt(fps) > 60) {
            int tempWidth2 = Integer.parseInt(width);
            int tempHeight2 = Integer.parseInt(height);
            resolutionPairs.add(tempWidth2 + "," + tempHeight2 + "," + "60");
            while ((tempWidth2 - tempWidth2 / 3) * (tempHeight2 - tempHeight2 / 3) >= 320 * 180) {
                tempWidth2 = tempWidth2 - tempWidth2 / 3;
                tempHeight2 = tempHeight2 - tempHeight2 / 3;
                // Asegurarse de que las dimensiones sean pares
                if (tempWidth2 % 2 != 0) {
                    tempWidth2--;
                }
                if (tempHeight2 % 2 != 0) {
                    tempHeight2--;
                }
                resolutionPairs.add(tempWidth2 + "," + tempHeight2 + "," + "60");
            }
        }

        // Crear los filtros
        List<String> filters = new ArrayList<>();

        String filtro = "[0:v]split=" + resolutionPairs.size();
        for (int i = 0; i < resolutionPairs.size(); i++) {
            filtro += "[v" + (i + 1) + "]";
        }
        filtro += ";";
        for (int i = 0; i < resolutionPairs.size(); i++) {
            if (i == 0) {
                filtro += " [v1]copy[v1out]";
            } else {
                filtro += "; [v" + (i + 1) + "]scale=w=" + resolutionPairs.get(i).split(",")[0] + ":h="
                        + resolutionPairs.get(i).split(",")[1] + "[v" + (i + 1) + "out]";
            }
        }
        filters.add(filtro);

        for (int i = 0; i < resolutionPairs.size(); i++) {
            int Width = Integer.parseInt(resolutionPairs.get(i).split(",")[0]);
            int Height = Integer.parseInt(resolutionPairs.get(i).split(",")[1]);
            int fpsn = Integer.parseInt(resolutionPairs.get(i).split(",")[2]);

            if (fpsn > 60 || Width * Height >= 3840 * 2160) {
                filters.addAll(Arrays.asList(
                        "-map", "[v" + (i + 1) + "out]",
                        "-c:v:" + i, "libaom-av1",
                        "-crf", "30",
                        "-b:v:" + i, "0",
                        "-cpu-used", "4",
                        "-g", String.valueOf(fpsn), // Conversión explícita de fps a String
                        "-sc_threshold", "0",
                        "-keyint_min", String.valueOf(fpsn),
                        "-hls_segment_filename", Width + "x" + Height + "@" + fpsn + "_%v/data%02d.ts",
                        "-hls_base_url", Width + "x" + Height + "@" + fpsn + "_" + i + "/"));
            } else {
                filters.addAll(Arrays.asList(
                        "-map", "[v" + (i + 1) + "out]",
                        "-c:v:" + i, "libx264",
                        "-preset", preset,
                        "-g", String.valueOf(fpsn), // Conversión explícita de fps a String
                        "-sc_threshold", "0",
                        "-keyint_min", String.valueOf(fpsn),
                        "-hls_segment_filename", Width + "x" + Height + "@" + fpsn + "_%v/data%02d.ts",
                        "-hls_base_url", Width + "x" + Height + "@" + fpsn + "_" + i + "/"));
            }
        }

        for (int i = 0; i < resolutionPairs.size(); i++) {
            int Width = Integer.parseInt(resolutionPairs.get(i).split(",")[0]);
            int Height = Integer.parseInt(resolutionPairs.get(i).split(",")[1]);
            int fpsn = Integer.parseInt(resolutionPairs.get(i).split(",")[2]);
            String audioBitrate = (Width * Height >= 1920 * 1080) ? "96k"
                    : (Width * Height >= 1280 * 720) ? "64k" : "48k";

            if (fpsn > 60) {
                filters.addAll(Arrays.asList(
                        "-map", "a:0", "-c:a:" + i, "aac", "-b:a:" + i, audioBitrate));
            } else {
                filters.addAll(Arrays.asList(
                        "-map", "a:0", "-c:a:" + i, "aac", "-b:a:" + i, audioBitrate));
            }
            if (i == 0) {
                filters.addAll(Arrays.asList("-ac", "2"));
            }
        }

        // Crea el comando FFmpeg
        List<String> ffmpegCommand = new ArrayList<>();
        ffmpegCommand = new ArrayList<>(List.of(
                "ffmpeg", "-loglevel", "warning"));
        if (live) {
            ffmpegCommand.add("-re");
        }
        ;
        ffmpegCommand.addAll(List.of(
                "-i", inputFilePath,
                "-f", "hls",
                "-hls_time", "5",
                "-hls_playlist_type", hls_playlist_type,
                "-hls_flags", hls_flags,
                "-hls_segment_type", "mpegts",
                "-filter_complex"));
        ffmpegCommand.addAll(filters);
        ffmpegCommand.addAll(List.of("-master_pl_name", "master.m3u8", "-var_stream_map"));
        String streamMap = "";
        for (int i = 0; i < resolutionPairs.size(); i++) {
            streamMap += " v:" + i + ",a:" + i;
        }
        ffmpegCommand.add(streamMap);
        ffmpegCommand.addAll(List.of(
                "stream_%v.m3u8"));
        if (live) {
            ffmpegCommand.addAll(List.of("-map", "0:v", "-map", "0:a", "-c:v", "copy", "-c:a", "aac", "original.mp4"));
        }
        return ffmpegCommand;
    }
}
