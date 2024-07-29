package com.sovereingschool.back_base.Controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sovereingschool.back_base.Interfaces.ICursoService;
import com.sovereingschool.back_base.Models.Clase;
import com.sovereingschool.back_base.Models.Curso;
import com.sovereingschool.back_base.Models.Plan;
import com.sovereingschool.back_base.Models.Usuario;

@RestController
@RequestMapping("/cursos")
@CrossOrigin(origins = "http://localhost:4200, https://giacca90.github.io")
public class CursoController {

	private String uploadDir = "/home/giacca90/Escritorio/Proyectos/SovereingSchool/Videos";

	@Autowired
	private ICursoService service;

	@GetMapping("/getAll")
	public ResponseEntity<?> getAll() {
		try {
			return new ResponseEntity<List<Curso>>(this.service.getAll(), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getCurso/{id}")
	public ResponseEntity<?> getCurso(@PathVariable Long id) {
		try {
			Curso curso = this.service.getCurso(id);
			if (curso == null)
				return new ResponseEntity<String>("Curso no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<Curso>(curso, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getNombreCurso/{id}")
	public ResponseEntity<?> getNombreCurso(@PathVariable Long id) {
		try {
			String nombre_curso = this.service.getNombreCurso(id);
			if (nombre_curso == null)
				return new ResponseEntity<String>("Curso no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<String>(nombre_curso, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getNombresProfesoresCurso/{id}")
	public ResponseEntity<?> getNombresProfesoresCurso(@PathVariable Long id) {
		try {
			List<Usuario> profesores = this.service.getProfesoresCurso(id);
			List<String> nombres_profesores = profesores.stream().map(Usuario::getNombre_usuario)
					.collect(Collectors.toList());
			if (nombres_profesores.isEmpty())
				return new ResponseEntity<String>("Curso no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<List<String>>(nombres_profesores, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getFechaCreacionCurso/{id}")
	public ResponseEntity<?> getFechaCreacionCurso(@PathVariable Long id) {
		try {
			Date fecha = this.service.getFechaCreacionCurso(id);
			if (fecha == null)
				return new ResponseEntity<String>("Curso no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<Date>(fecha, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getClasesDelCurso/{id}")
	public ResponseEntity<?> getClasesDelCurso(@PathVariable Long id) {
		try {
			List<Clase> clases = this.service.getClasesDelCurso(id);
			if (clases.isEmpty())
				return new ResponseEntity<String>("Curso no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<List<Clase>>(clases, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getPlanesDelCurso/{id}")
	public ResponseEntity<?> getPlanesDelCurso(@PathVariable Long id) {
		try {
			List<Plan> planes = this.service.getPlanesDelCurso(id);
			if (planes.isEmpty())
				return new ResponseEntity<String>("Curso no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<List<Plan>>(planes, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getPrecioCurso/{id}")
	public ResponseEntity<?> getPrecioCurso(@PathVariable Long id) {
		try {
			BigDecimal precio = this.service.getPrecioCurso(id);
			if (precio == null)
				return new ResponseEntity<String>("Curso no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<BigDecimal>(precio, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/new")
	public ResponseEntity<?> createCurso(@RequestBody Curso curso) {
		try {
			return new ResponseEntity<String>(this.service.createCurso(curso), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/subeVideo")
	public ResponseEntity<?> create(@RequestParam("video") MultipartFile file) {
		try {
			if (file.isEmpty()) {
				return new ResponseEntity<>("Archivo vacío", HttpStatus.BAD_REQUEST);
			}
			// Obtiene el nombre del archivo
			String fileName = UUID.randomUUID().toString() + "_"
					+ StringUtils.cleanPath(file.getOriginalFilename()).replaceAll(" ", "_");
			// Define el path para guardar el archivo
			Path path = Paths.get(uploadDir, fileName);

			// Guarda el archivo en el servidor
			Files.write(path, file.getBytes());

			return new ResponseEntity<String>("http://localhost:8080/Videos/" + fileName, HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>("Error en subir el video: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/update")
	public ResponseEntity<Map<String, Object>> updateCurso(@RequestBody Curso curso) {
		Map<String, Object> response = new HashMap<>();
		try {
			Curso result = this.service.updateCurso(curso);
			if (result == null) {
				response.put("message", "Curso no encontrado");
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}

			response.put("message", "Curso actualizado con éxito!!!");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.put("message", "Error interno del servidor");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteCurso(@PathVariable Long id) {
		try {
			String result = this.service.deleteCurso(id);
			if (result == null)
				return new ResponseEntity<String>("Curso no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<String>("Curso eliminado con éxito!!!", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/{idCurso}/getClaseForId/{idClase}")
	public ResponseEntity<?> getClaseForId(@PathVariable Long idCurso, @PathVariable Long idClase) {
		try {
			Curso curso = this.service.getCurso(idCurso);
			if (curso == null)
				return new ResponseEntity<String>("Curso no encontrado", HttpStatus.NOT_FOUND);
			List<Clase> clases = curso.getClases_curso();
			for (Clase clase : clases) {
				if (clase.getId_clase().equals(idClase))
					return new ResponseEntity<Clase>(clase, HttpStatus.OK);
			}
			return new ResponseEntity<String>("Clase no encontrada", HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/{idCurso}/editClase")
	public ResponseEntity<?> update(@PathVariable Long idCurso, @RequestBody Clase clase) {
		try {
			Curso curso = this.service.getCurso(idCurso);
			if (curso == null)
				return new ResponseEntity<String>("Curso no encontrado", HttpStatus.NOT_FOUND);
			List<Clase> clases = curso.getClases_curso();
			for (Clase claseVieja : clases) {
				if (claseVieja.getId_clase().equals(clase.getId_clase())) {
					clases.remove(claseVieja);
					clase.setCurso_clase(curso);
					clases.add(clase);
					curso.setClases_curso(clases);
					this.service.updateCurso(curso);
					return new ResponseEntity<String>("Clase editada con éxito!!!", HttpStatus.OK);
				}
			}
			return new ResponseEntity<String>("Clase no encontrada", HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/{idCurso}/addClase")
	public ResponseEntity<?> addClase(@PathVariable Long idCurso, @RequestBody Clase clase) {
		try {
			Curso curso = this.service.getCurso(idCurso);
			if (curso == null)
				return new ResponseEntity<String>("Curso no encontrado", HttpStatus.NOT_FOUND);
			List<Clase> clases = curso.getClases_curso();
			if (clase.getPosicion_clase() == null || clase.getPosicion_clase() == 0)
				clase.setPosicion_clase((clases.size()) + 1);
			else
				clases.forEach((Clase fclase) -> {
					if (fclase.getPosicion_clase() == clase.getPosicion_clase())
						clase.setPosicion_clase((clases.size()) + 1);
				});
			clase.setCurso_clase(curso);
			clases.add(clase);
			curso.setClases_curso(clases);
			this.service.updateCurso(curso);
			return new ResponseEntity<>("Clase añadida con éxito!!!", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/{idCurso}/deleteClase/{idClase}")
	public ResponseEntity<?> deleteClase(@PathVariable Long idCurso, @PathVariable Long idClase) {
		try {
			Curso curso = this.service.getCurso(idCurso);
			if (curso == null)
				return new ResponseEntity<String>("Curso no encontrado", HttpStatus.NOT_FOUND);
			List<Clase> clases = curso.getClases_curso();
			Clase eliminada = null;
			for (int i = 0; i < clases.size(); i++) {
				if (eliminada != null) {
					clases.get(i).setPosicion_clase(clases.get(i).getPosicion_clase() - 1);
				} else {
					if (clases.get(i).getId_clase() == idClase) {
						eliminada = clases.get(i);
					}
				}
			}

			if (eliminada != null) {
				clases.remove(eliminada);
				curso.setClases_curso(clases);
				this.service.updateCurso(curso);
				this.service.deleteClase(eliminada);
				return new ResponseEntity<String>("Clase eliminada con exito!!!", HttpStatus.OK);
			}
			return new ResponseEntity<>("Clase no encontrada", HttpStatus.NOT_FOUND);

		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
