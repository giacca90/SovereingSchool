package com.sovereingschool.back_base.Configurations;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.sovereingschool.back_base.Services.CursoService;

public class WebRTCSignalingHandler extends BinaryWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private PipedInputStream inputStream;
    private PipedOutputStream outputStream;
    private boolean ffmpegProcessRunning = false;

    private CursoService cursoService; // Aquí lo declaramos

    // Constructor con CursoService como dependencia
    public WebRTCSignalingHandler(CursoService cursoService) {
        try {
            this.cursoService = cursoService;
            this.inputStream = new PipedInputStream();
            this.outputStream = new PipedOutputStream(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error inicializando los flujos de datos para FFmpeg", e);
        }
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("Nueva conexión: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("Conexión cerrada: " + session.getId() + " Razón: " + status.getReason());
    }

    @Override
    protected void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message)
            throws Exception {
        if (!ffmpegProcessRunning) {
            startFFmpegProcess();
        }

        // Log para verificar el tamaño del mensaje recibido
        byte[] payload = message.getPayload().array();
        System.out.println("Mensaje recibido desde WebSocket, tamaño de payload: " + payload.length);

        /*
         * // Guardar el payload recibido en un archivo para depuración
         * try (FileOutputStream fos = new FileOutputStream("payload_received.ts",
         * true)) {
         * fos.write(payload);
         * fos.flush();
         * System.out.println("Payload recibido guardado en payload_received.ts");
         * }
         */

        // Escribir los datos recibidos al stream conectado a FFmpeg
        try {
            outputStream.write(payload);
            outputStream.flush();
            System.out.println("Datos enviados a FFmpeg, tamaño: " + payload.length);
        } catch (IOException e) {
            e.printStackTrace();
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private void startFFmpegProcess() {
        ffmpegProcessRunning = true;
        new Thread(() -> {
            try {
                // Verificamos si el flujo de datos está recibiendo algo
                System.out.println("Iniciando proceso FFmpeg...");

                if (cursoService != null) {
                    System.out.println("Llamando a startLiveStreamingFromStream...");
                    cursoService.startLiveStreamingFromStream(inputStream);
                } else {
                    System.out.println("CursoService no está inyectado correctamente");
                }
            } catch (IOException e) {
                System.out.println("Error al iniciar FFmpeg: " + e.getMessage());
                e.printStackTrace();
            } finally {
                ffmpegProcessRunning = false;
            }
        }).start();
    }

}
