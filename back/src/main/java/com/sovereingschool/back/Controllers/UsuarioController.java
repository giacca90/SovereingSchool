package com.sovereingschool.back.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sovereingschool.back.Interfaces.IUsuarioService;
import com.sovereingschool.back.Models.Curso;
import com.sovereingschool.back.Models.Plan;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

	@Autowired
	private IUsuarioService service;

	@GetMapping("/nombre/{id}")
	public ResponseEntity<?> getNombreUsuario(@PathVariable Long id) {
		try {
			return new ResponseEntity<String>(this.service.getNombreUsuario(id), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/fotos/{id}")
	public ResponseEntity<?> getFotosUsuario(@PathVariable Long id) {
		try {
			return new ResponseEntity<List<String>>(this.service.getFotosUsuario(id), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/roll/{id}")
	public ResponseEntity<?> getRollUsuario(@PathVariable Long id) {
		try {
			return new ResponseEntity<Integer>(this.service.getRollUsuario(id), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/plan/{id}")
	public ResponseEntity<?> getPlanUsuario(@PathVariable Long id) {
		try {
			return new ResponseEntity<Plan>(this.service.getPlanUsuario(id), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/cursos/{id}")
	public ResponseEntity<?> getCursosUsuario(@PathVariable Long id) {
		try {
			return new ResponseEntity<List<Curso>>(this.service.getCursosUsuario(id), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/nuevo/{nombre_usuario}&{fotos_usuario}&{plan_usuario}&{cursos_usuario}")
	public ResponseEntity<?> createUsuario(@PathVariable String nombre_usuario,
			@PathVariable List<String> fotos_usuario, @PathVariable Plan plan_usuario,
			@PathVariable List<Curso> cursos_usuario) {
		try {
			return new ResponseEntity<String>(
					this.service.createUsuario(nombre_usuario, fotos_usuario, plan_usuario, cursos_usuario),
					HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/nombre/{id_usuario}&{new_nombre_usuario}")
	public ResponseEntity<?> changeNombreUsuario(@PathVariable Long id_usuario,
			@PathVariable String new_nombre_usuario) {
		try {
			return new ResponseEntity<String>(this.service.changeNombreUsuario(id_usuario, new_nombre_usuario),
					HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/fotos/{id_usuario}&{fotos_usuario}")
	public ResponseEntity<?> changeFotosUsuario(@PathVariable Long id_usuario,
			@PathVariable List<String> fotos_usuario) {
		try {
			return new ResponseEntity<String>(this.service.changeFotosUsuario(id_usuario, fotos_usuario),
					HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/plan/{id_usuario}&{plan}")
	public ResponseEntity<?> changePlanUsuario(@PathVariable Long id_usuario, @PathVariable Plan plan) {
		try {
			return new ResponseEntity<String>(this.service.changePlanUsuario(id_usuario, plan), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/cursos/{id_usuario}&{cursos_usuario}")
	public ResponseEntity<?> changeCursosUsuario(@PathVariable Long id_usuario,
			@PathVariable List<Curso> cursos_usuario) {
		try {
			return new ResponseEntity<String>(this.service.changeCursosUsuario(id_usuario, cursos_usuario),
					HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
		try {
			return new ResponseEntity<String>(this.service.deleteUsuario(id), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
