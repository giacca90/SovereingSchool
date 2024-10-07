package com.sovereingschool.back_base.Controllers;

import java.awt.image.BufferedImage;
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

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
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

	private String uploadDir = "/home/matt/Escritorio/Proyectos/SovereingSchool/Fotos";

	@GetMapping("/{id}")
	public ResponseEntity<?> getUsuario(@PathVariable Long id) {
		Object response = new Object();
		try {
			Usuario usuario = this.service.getUsuario(id);
			if (usuario == null) {
				response = "Usuario no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = usuario;
			return new ResponseEntity<>(usuario, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en buscar el usuario: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/nombre/{id}")
	public ResponseEntity<?> getNombreUsuario(@PathVariable Long id) {
		Object response = new Object();
		try {
			String nombre = this.service.getNombreUsuario(id);
			if (nombre == null) {
				response = "Usuario no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = nombre;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en encontrar el nombre del usuario: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/fotos/{nombreFoto}")
	public ResponseEntity<?> getFotos(@PathVariable String nombreFoto) {
		final String photosDirectory = "/home/matt/Escritorio/Proyectos/SovereingSchool/Fotos";
		Object response = new Object();

		// Construir la ruta del archivo y resolver posibles vulnerabilidades de
		// directorio transversal
		Path photoPath = Paths.get(photosDirectory).resolve(nombreFoto).normalize();
		File photoFile = photoPath.toFile();

		if (!photoFile.exists() || !photoFile.isFile()) {
			response = "Foto no encontrada.";
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}

		// Construir el recurso a partir del archivo
		FileSystemResource resource = new FileSystemResource(photoFile);

		// Determinar el tipo de contenido
		String contentType;
		try {
			contentType = Files.probeContentType(photoPath);
		} catch (IOException e) {
			response = "Error determinando el tipo de contenido." + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// Configurar cache control
		CacheControl cacheControl = CacheControl.maxAge(30, TimeUnit.DAYS); // 30 días de caché

		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl(cacheControl.toString());
		headers.setContentType(
				contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDisposition(ContentDisposition.inline().filename(resource.getFilename()).build());
		response = resource;
		return new ResponseEntity<>(response, headers, HttpStatus.OK);
	}

	@GetMapping("/roll/{id}")
	public ResponseEntity<?> getRollUsuario(@PathVariable Long id) {
		Object response = new Object();
		try {
			Integer roll = this.service.getRollUsuario(id);
			if (roll == null) {
				response = "Usuario no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = roll;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener el roll del usuario: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/plan/{id}")
	public ResponseEntity<?> getPlanUsuario(@PathVariable Long id) {
		Object response = new Object();
		try {
			Plan plan = this.service.getPlanUsuario(id);
			if (plan == null) {
				response = "Usuario no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = plan;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener el plan del usuario: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/cursos/{id}")
	public ResponseEntity<?> getCursosUsuario(@PathVariable Long id) {
		Object response = new Object();
		try {
			List<Curso> cursos = this.service.getCursosUsuario(id);
			if (cursos == null) {
				response = "Usuario no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = cursos;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener los cursos del usuario: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/nuevo")
	public ResponseEntity<?> createUsuario(@RequestBody NewUsuario newUsuario) {
		Object response = new Object();
		try {
			response = this.service.createUsuario(newUsuario);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en crear el nuevo usuario: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/edit")
	public ResponseEntity<?> editUsuario(@RequestBody Usuario usuario) {
		Object response = new Object();
		try {
			Long resultado = this.service.updateUsuario(usuario).getId_usuario();
			if (resultado == 0) {
				response = "Usuario no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = "Usuario editado con éxito!!!";
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en editar el usuario: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/plan")
	public ResponseEntity<?> changePlanUsuario(@RequestBody Usuario usuario) {
		Object response = new Object();
		try {
			Integer resultado = this.service.changePlanUsuario(usuario);
			if (resultado == 0) {
				response = "Usuario no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = "Plan cambiado con éxito!!!";
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en cambiar el plan del usuario: " + e.getMessage();
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/cursos")
	public ResponseEntity<?> changeCursosUsuario(@RequestBody Usuario usuario) {
		Object response = new Object();
		try {
			Integer resultado = this.service.changeCursosUsuario(usuario);
			if (resultado == 0) {
				response = "Usuario no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = "Cursos actualizados con éxito!!!";
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en actualizar los cursos del usuario: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
		Object response = new Object();
		try {
			String result = this.service.deleteUsuario(id);
			if (result == null) {
				response = "Usuario no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = result;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en eliminar el usuario: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/profes")
	public ResponseEntity<?> getProfes() {
		Object response = new Object();
		try {
			response = this.service.getProfes();
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener los profesores: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/subeFotos")
	public ResponseEntity<?> uploadImages(@RequestBody MultipartFile[] files) {
		System.out.println("Se suben fotos");
		List<String> fileNames = new ArrayList<>();
		Object response = new Object();

		for (MultipartFile file : files) {
			// Genera un nombre único para cada archivo para evitar colisiones
			String fileName = UUID.randomUUID().toString() + "_"
					+ StringUtils.cleanPath(file.getOriginalFilename()).replaceAll(" ", "_");
			Path filePath = Paths.get(uploadDir, fileName);
			System.out.println("Foto: " + filePath.toString());
			if (!fileName.substring(fileName.lastIndexOf(".")).toLowerCase().equals(".svg")) {

				try {
					// Convertir la imagen a WebP
					BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
					BufferedImage webpImage = Scalr.resize(bufferedImage, Scalr.Method.QUALITY,
							bufferedImage.getWidth(),
							bufferedImage.getHeight());
					String webpFileName = fileName.replaceFirst("[.][^.]+$", "") + ".webp";
					Path webpFilePath = Paths.get(uploadDir, webpFileName);
					Files.createDirectories(webpFilePath.getParent());
					ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
					try (ImageOutputStream ios = ImageIO.createImageOutputStream(webpFilePath.toFile())) {
						writer.setOutput(ios);
						ImageWriteParam param = writer.getDefaultWriteParam();
						writer.write(null, new IIOImage(webpImage, null, null), param);
					}
					// Files.copy(file.getInputStream(), filePath,
					// StandardCopyOption.REPLACE_EXISTING);
					fileNames.add("http://localhost:8080/usuario/fotos/" + webpFileName);
				} catch (IOException e) {
					response = "Error en convertir la imagen: " + e.getMessage();
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				try {
					Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
					fileNames.add("http://localhost:8080/usuario/fotos/" + fileName);
				} catch (IOException e) {
					response = "Error en guardar la imagen: " + e.getMessage();
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}
		response = fileNames;
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
