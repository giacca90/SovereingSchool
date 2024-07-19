package com.sovereingschool.back_base.Controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sovereingschool.back_base.DTOs.NewUsuario;
import com.sovereingschool.back_base.Interfaces.IUsuarioService;
import com.sovereingschool.back_base.Models.Curso;
import com.sovereingschool.back_base.Models.Plan;
import com.sovereingschool.back_base.Models.Usuario;

@RestController
@RequestMapping("/usuario")
@CrossOrigin(origins = "http://localhost:4200, https://giacca90.github.io")
public class UsuarioController {

	@Autowired
	private IUsuarioService service;

	private String uploadDir = "/home/giacca90/Escritorio/Proyectos/SovereingSchool/Fotos";

	@GetMapping("/{id}")
	public ResponseEntity<?> getUsuario(@PathVariable Long id) {
		try {
			Usuario usuario = this.service.getUsuario(id);
			if (usuario == null)
				return new ResponseEntity<String>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<Usuario>(usuario, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/nombre/{id}")
	public ResponseEntity<?> getNombreUsuario(@PathVariable Long id) {
		try {
			String nombre = this.service.getNombreUsuario(id);
			if (nombre == null)
				return new ResponseEntity<String>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<String>(nombre, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/fotos/{nombreFoto}")
	public ResponseEntity<?> getFotos(@PathVariable String nombreFoto) {
		System.out.println("LOG. " + nombreFoto);
		final String photosDirectory = "/home/giacca90/Escritorio/Proyectos/SovereingSchool/Fotos";

		// Construir la ruta del archivo y resolver posibles vulnerabilidades de
		// directorio transversal
		Path photoPath = Paths.get(photosDirectory).resolve(nombreFoto).normalize();
		File photoFile = photoPath.toFile();
		System.out.println("LOG2: " + photoFile.getAbsolutePath());

		if (!photoFile.exists() || !photoFile.isFile()) {
			return new ResponseEntity<String>("Foto no encontrada.", HttpStatus.NOT_FOUND);
		}

		// Construir el recurso a partir del archivo
		FileSystemResource resource = new FileSystemResource(photoFile);

		// Determinar el tipo de contenido
		String contentType;
		try {
			contentType = Files.probeContentType(photoPath);
		} catch (IOException e) {
			return new ResponseEntity<String>("Error determinando el tipo de contenido.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// Configurar cache control
		CacheControl cacheControl = CacheControl.maxAge(30, TimeUnit.DAYS); // 30 días de caché

		return ResponseEntity.ok()
				.cacheControl(cacheControl)
				.contentType(contentType != null ? MediaType.parseMediaType(contentType)
						: MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	@GetMapping("/roll/{id}")
	public ResponseEntity<?> getRollUsuario(@PathVariable Long id) {
		try {
			Integer roll = this.service.getRollUsuario(id);
			if (roll == null)
				return new ResponseEntity<String>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<Integer>(roll, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/plan/{id}")
	public ResponseEntity<?> getPlanUsuario(@PathVariable Long id) {
		try {
			Plan plan = this.service.getPlanUsuario(id);
			if (plan == null)
				return new ResponseEntity<String>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<Plan>(plan, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/cursos/{id}")
	public ResponseEntity<?> getCursosUsuario(@PathVariable Long id) {
		try {
			List<Curso> cursos = this.service.getCursosUsuario(id);
			if (cursos == null)
				return new ResponseEntity<String>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<List<Curso>>(cursos, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/nuevo")
	public ResponseEntity<?> createUsuario(@RequestBody NewUsuario newUsuario) {
		try {
			return new ResponseEntity<String>(this.service.createUsuario(newUsuario), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/edit")
	public ResponseEntity<?> editUsuario(@RequestBody Usuario usuario) {
		System.out.println("SE ACTUALIZA USUARIO");
		try {
			Long resultado = this.service.updateUsuario(usuario).getId_usuario();
			if (resultado == 0)
				return new ResponseEntity<String>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<String>("Usuario editado con éxito!!!", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/plan")
	public ResponseEntity<?> changePlanUsuario(@RequestBody Usuario usuario) {
		try {
			Integer resultado = this.service.changePlanUsuario(usuario);
			if (resultado == 0)
				return new ResponseEntity<String>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<String>("Plan cambiado con éxito!!!", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/cursos")
	public ResponseEntity<?> changeCursosUsuario(@RequestBody Usuario usuario) {
		try {
			Integer resultado = this.service.changeCursosUsuario(usuario);
			if (resultado == 0)
				return new ResponseEntity<String>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<String>("Cursos actualizados con éxito!!!", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
		try {
			String result = this.service.deleteUsuario(id);
			if (result == null)
				return new ResponseEntity<String>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<String>(result, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/profes")
	public ResponseEntity<?> getProfes() {
		try {
			return new ResponseEntity<List<Usuario>>(this.service.getProfes(), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/subeFotos")
	public ResponseEntity<List<String>> uploadImages(@RequestBody MultipartFile[] files) {
		System.out.println("Se suben fotos");
		List<String> fileNames = new ArrayList<>();

		for (MultipartFile file : files) {
			// Genera un nombre único para cada archivo para evitar colisiones
			String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
			Path filePath = Paths.get(uploadDir, fileName);
			System.out.println("Foto: " + filePath.toString());

			try {
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				fileNames.add("http://localhost:8080/usuario/fotos/" + fileName);
			} catch (IOException e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		}

		return ResponseEntity.ok(fileNames);
	}
}
