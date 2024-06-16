package com.sovereingschool.back.Controllers;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.sovereingschool.back.Interfaces.ICursoService;
import com.sovereingschool.back.Models.Clase;
import com.sovereingschool.back.Models.Curso;
import com.sovereingschool.back.Models.Plan;

@RestController
@RequestMapping("/cursos")
public class CursoController {

	@Autowired
	private ICursoService service;

	@GetMapping("/getCurso/{id}")
	public ResponseEntity<?> getCurso(@PathVariable Long id) {
		try {
			return new ResponseEntity<Curso>(this.service.getCurso(id), HttpStatus.OK);
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
			List<String> nombres_profesores = this.service.getNombresProfesoresCurso(id);
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
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/update")
	public ResponseEntity<?> updateCurso(@RequestBody Curso curso) {
		try {
			return new ResponseEntity<>(this.service.updateCurso(curso), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteCurso(@PathVariable Long id) {
		try {
			return new ResponseEntity<String>(this.service.deleteCurso(id), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
