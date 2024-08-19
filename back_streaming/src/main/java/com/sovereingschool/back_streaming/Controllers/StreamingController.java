package com.sovereingschool.back_streaming.Controllers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.sovereingschool.back_streaming.Models.Clase;
import com.sovereingschool.back_streaming.Models.Usuario;
import com.sovereingschool.back_streaming.Services.UsuarioCursosService;

@RestController
@CrossOrigin(origins = "http://localhost:4200, http://localhost:8080 https://giacca90.github.io")
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

    @Autowired
    private UsuarioCursosService usuarioCursosService;

    @GetMapping("/{id_usuario}/{id_curso}/{id_clase}/{lista}")
    public ResponseEntity<InputStreamResource> getListas(@PathVariable Long id_usuario, @PathVariable Long id_curso,
            @PathVariable Long id_clase,
            @PathVariable String lista,
            @RequestHeader HttpHeaders headers) throws IOException {
        System.out.println("LOG");

        String direccion_carpeta = this.usuarioCursosService.getClase(id_usuario, id_curso, id_clase);
        System.out.println("LOG2: " + direccion_carpeta);
        if (direccion_carpeta == null) {
            System.err.println("El video no tiene ruta");
            return ResponseEntity.notFound().build();
        }

        Path carpetaPath = Paths.get(direccion_carpeta);

        Path videoPath = carpetaPath.resolve(lista);

        System.out.println("LOG 3" + videoPath.toString());
        if (!Files.exists(videoPath)) {
            System.err.println("No existe el archivo: " + videoPath);
            return ResponseEntity.notFound().build();
        }

        // Obtener el tipo MIME del video
        String contentType = Files.probeContentType(videoPath);
        System.out.println("LOG4: " + contentType);

        // Configurar las cabeceras de la respuesta
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(contentType));
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "inline");
        responseHeaders.add(HttpHeaders.CACHE_CONTROL, "no-store");
        responseHeaders.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);

        long fileLength = Files.size(videoPath);
        System.out.println("Log5: " + videoPath.toString().substring(videoPath.toString().lastIndexOf(".")));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(fileLength)
                .headers(responseHeaders)
                .body(new InputStreamResource(Files.newInputStream(videoPath)));

    }

    @GetMapping("/{id_usuario}/{id_curso}/{id_clase}/{lista}/{video}")
    public ResponseEntity<InputStreamResource> streamVideo(@PathVariable Long id_usuario, @PathVariable Long id_curso,
            @PathVariable Long id_clase,
            @PathVariable String lista,
            @PathVariable String video,
            @RequestHeader HttpHeaders headers) throws IOException {
        System.out.println("LOG");

        String direccion_carpeta = this.usuarioCursosService.getClase(id_usuario, id_curso, id_clase);
        System.out.println("LOG2: " + direccion_carpeta);
        if (direccion_carpeta == null) {
            System.err.println("El video no tiene ruta");
            return ResponseEntity.notFound().build();
        }

        Path carpetaPath = Paths.get(direccion_carpeta);

        Path videoPath = carpetaPath.resolve(lista);

        videoPath = videoPath.resolve(video);

        System.out.println("LOG 3" + videoPath.toString());
        if (!Files.exists(videoPath)) {
            System.err.println("No existe el archivo: " + videoPath);
            return ResponseEntity.notFound().build();
        }

        // Obtener el tipo MIME del video
        String contentType = Files.probeContentType(videoPath);
        System.out.println("LOG4: " + contentType);

        // Configurar las cabeceras de la respuesta
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(contentType));
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "inline");
        responseHeaders.add(HttpHeaders.CACHE_CONTROL, "no-store");
        responseHeaders.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);

        long fileLength = Files.size(videoPath);
        List<HttpRange> ranges = headers.getRange();
        if (ranges.isEmpty()) {
            System.out.println("Log5: " + videoPath.toString().substring(videoPath.toString().lastIndexOf(".")));
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileLength)
                    .headers(responseHeaders)
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
                .headers(responseHeaders)
                .body(resource);
    }

    @GetMapping("/init")
    public ResponseEntity<?> get() {
        try {
            this.usuarioCursosService.syncUserCourses();
            return new ResponseEntity<String>("Iniciado mongo con exito!!!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{id_usuario}/{id_curso}")
    public ResponseEntity<?> getStatus(@PathVariable Long id_usuario, @PathVariable Long id_curso) {
        try {
            return new ResponseEntity<Long>(this.usuarioCursosService.getStatus(id_usuario, id_curso), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error en obtener la clase: " + e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/nuevoUsuario")
    public ResponseEntity<?> create(@RequestBody Usuario usuario) {
        try {
            return new ResponseEntity<>(this.usuarioCursosService.addNuevoUsuario(usuario), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/addClase/{idCurso}")
    public ResponseEntity<?> add(@PathVariable Long idCurso, @RequestBody Clase clase) {
        try {
            if (this.usuarioCursosService.addClase(idCurso, clase)) {
                return new ResponseEntity<Boolean>(true, HttpStatus.OK);
            } else {
                return new ResponseEntity<Boolean>(false, HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getCause().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteClase/{idCurso}/{idClase}")
    public ResponseEntity<?> update(@PathVariable Long idCurso, @PathVariable Long idClase) {
        try {
            if (this.usuarioCursosService.deleteClase(idCurso, idClase)) {
                return new ResponseEntity<Boolean>(true, HttpStatus.OK);
            } else {
                return new ResponseEntity<Boolean>(false, HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getCause().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
