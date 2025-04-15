package com.sovereingschool.back_base.Controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class CursoController {
	@Autowired
	private ICursoService service;

	/* Parte de gestión de cursos */
	@GetMapping("/getAll")
	public ResponseEntity<?> getAll() {
		Object response = new Object();
		try {
			response = this.service.getAll();
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener los cursos: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getCurso/{id}")
	public ResponseEntity<?> getCurso(@PathVariable Long id) {
		Object response = new Object();
		try {
			Curso curso = this.service.getCurso(id);
			if (curso == null) {
				response = "Curso no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = curso;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener el curso: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getNombreCurso/{id}")
	public ResponseEntity<?> getNombreCurso(@PathVariable Long id) {
		Object response = new Object();
		try {
			String nombre_curso = this.service.getNombreCurso(id);
			if (nombre_curso == null) {
				response = "Curso no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = nombre_curso;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener el nombre del curso: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getNombresProfesoresCurso/{id}")
	public ResponseEntity<?> getNombresProfesoresCurso(@PathVariable Long id) {
		Object response = new Object();
		try {
			List<Usuario> profesores = this.service.getProfesoresCurso(id);
			List<String> nombres_profesores = profesores.stream().map(Usuario::getNombre_usuario)
					.collect(Collectors.toList());
			if (nombres_profesores.isEmpty()) {
				response = "Curso no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = nombres_profesores;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener los nombres de los profesores: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getFechaCreacionCurso/{id}")
	public ResponseEntity<?> getFechaCreacionCurso(@PathVariable Long id) {
		Object response = new Object();
		try {
			Date fecha = this.service.getFechaCreacionCurso(id);
			if (fecha == null) {
				response = "Curso no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = fecha;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener la fecha del curso: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getClasesDelCurso/{id}")
	public ResponseEntity<?> getClasesDelCurso(@PathVariable Long id) {
		Object response = new Object();
		try {
			List<Clase> clases = this.service.getClasesDelCurso(id);
			if (clases.isEmpty()) {
				response = "Curso no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}

			response = clases;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener las clases del curso: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getPlanesDelCurso/{id}")
	public ResponseEntity<?> getPlanesDelCurso(@PathVariable Long id) {
		Object response = new Object();
		try {
			List<Plan> planes = this.service.getPlanesDelCurso(id);
			if (planes.isEmpty()) {
				response = "Curso no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = planes;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener los planes del curso: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getPrecioCurso/{id}")
	public ResponseEntity<?> getPrecioCurso(@PathVariable Long id) {
		Object response = new Object();
		try {
			BigDecimal precio = this.service.getPrecioCurso(id);
			if (precio == null) {
				response = "Curso no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = precio;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener el precio del curso: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/update")
	public ResponseEntity<?> updateCurso(@RequestBody Curso curso) {
		Object response = new Object();
		try {
			Curso result = this.service.updateCurso(curso);
			if (result == null) {
				response = "Curso no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = "Curso actualizado con éxito!!!";
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en actualizar el curso: " + e.getMessage();
			return new ResponseEntity<>(response,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteCurso(@PathVariable Long id) {
		Object response = new Object();
		try {
			Boolean result = this.service.deleteCurso(id);
			if (result == false) {
				response = "Curso no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = "Curso eliminado con éxito!!!";
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en eliminar el curso: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/* Parte de gestión de clases */

	@GetMapping("/{idCurso}/getClaseForId/{idClase}")
	public ResponseEntity<?> getClaseForId(@PathVariable Long idCurso, @PathVariable Long idClase) {
		Object response = new Object();
		try {
			Curso curso = this.service.getCurso(idCurso);
			if (curso == null) {
				response = "Curso eliminado con éxito!!!";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			List<Clase> clases = curso.getClases_curso();
			for (Clase clase : clases) {
				if (clase.getId_clase().equals(idClase)) {
					response = clase;
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			response = "Clase no encontrada";
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			response = "Error en encontrar la clase: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/{idCurso}/deleteClase/{idClase}")
	public ResponseEntity<?> deleteClase(@PathVariable Long idCurso, @PathVariable Long idClase) {
		Object response = new Object();
		try {
			Curso curso = this.service.getCurso(idCurso);
			if (curso == null) {
				response = "Curso no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
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
				response = "Clase eliminada con exito!!!";
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			response = "Clase no encontrada";
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

		} catch (Exception e) {
			response = "Error en borrar la clase: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/subeVideo/{idCurso}/{idClase}")
	public ResponseEntity<?> subeVideo(@PathVariable Long idCurso, @PathVariable Long idClase,
			@RequestParam("video") MultipartFile file)
			throws IOException {
		Object response = new Object();
		if (file.isEmpty()) {
			response = "Archivo vacío";
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		String filePath = this.service.subeVideo(file);
		if (filePath == null) {
			response = "Error al subir el video";
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response = filePath.toString();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
