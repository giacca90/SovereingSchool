package com.sovereingschool.back_streaming.Controllers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StreamingController {

    private static class LimitedInputStream extends java.io.InputStream {
        private final RandomAccessFile file;
        private long remaining;

        public LimitedInputStream(RandomAccessFile file, long remaining) {
            this.file = file;
            this.remaining = remaining;
        }

        @Override
        public int read() throws IOException {
            if (remaining > 0) {
                remaining--;
                return file.read();
            } else {
                return -1;
            }
        }
    }

    @GetMapping("/clases/{direccion_video}")
    public ResponseEntity<InputStreamResource> streamVideo(@PathVariable String direccion_video,
            @RequestHeader HttpHeaders headers) throws IOException {
        Path videoPath = Paths.get(direccion_video);
        if (!Files.exists(videoPath)) {
            return ResponseEntity.notFound().build();
        }

        long fileLength = Files.size(videoPath);
        List<HttpRange> ranges = headers.getRange();
        if (ranges.isEmpty()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileLength)
                    .body(new InputStreamResource(Files.newInputStream(videoPath)));
        }

        HttpRange range = ranges.get(0);
        long start = range.getRangeStart(0);
        long end = range.getRangeEnd(fileLength - 1);
        if (end > fileLength - 1) {
            end = fileLength - 1;
        }
        long rangeLength = end - start + 1;

        RandomAccessFile file = new RandomAccessFile(videoPath.toFile(), "r");
        file.seek(start);

        InputStreamResource resource = new InputStreamResource(new LimitedInputStream(file, rangeLength));

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(rangeLength))
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                .body(resource);
    }
}

/*
 * Explicación del código:
 * 
 * @GetMapping("/videos/{filename}"): Define una ruta para las solicitudes GET
 * que incluyan el nombre del archivo de video.
 * 
 * @RequestHeader HttpHeaders headers: Obtiene los encabezados de la solicitud
 * HTTP, que incluyen información sobre los rangos solicitados para el video.
 * Files.exists(videoPath): Verifica si el archivo de video existe en la ruta
 * especificada.
 * Files.size(videoPath): Obtiene el tamaño del archivo de video.
 * headers.getRange(): Obtiene la lista de rangos solicitados de los encabezados
 * HTTP.
 * HttpRange: Maneja los rangos de bytes especificados para el streaming.
 * RandomAccessFile: Permite el acceso a diferentes partes del archivo de video.
 * LimitedInputStream: Clase interna para limitar la cantidad de datos leídos
 * del archivo.
 */

/*
 * @RestController
 * 
 * @RequestMapping("/api/user-courses")
 * public class UserCoursesController {
 * 
 * @Autowired
 * private UserCoursesRepository userCoursesRepository;
 * 
 * @Autowired
 * private SyncService syncService;
 * 
 * @GetMapping("/{userId}")
 * public ResponseEntity<UserCourses> getUserCourses(@PathVariable String
 * userId) {
 * UserCourses userCourses = userCoursesRepository.findByUserId(userId);
 * if (userCourses != null) {
 * return ResponseEntity.ok(userCourses);
 * } else {
 * return ResponseEntity.notFound().build();
 * }
 * }
 * 
 * @PostMapping("/sync")
 * public ResponseEntity<Void> syncUserCourses() {
 * syncService.syncUserCourses();
 * return ResponseEntity.ok().build();
 * }
 * }
 */