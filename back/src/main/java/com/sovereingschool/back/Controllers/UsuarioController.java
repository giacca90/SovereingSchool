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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sovereingschool.back.DTOs.NewUsuario;
import com.sovereingschool.back.Interfaces.IUsuarioService;
import com.sovereingschool.back.Models.Curso;
import com.sovereingschool.back.Models.Plan;
import com.sovereingschool.back.Models.Usuario;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

	@Autowired
	private IUsuarioService service;

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

	@GetMapping("/fotos/{id}")
	public ResponseEntity<?> getFotosUsuario(@PathVariable Long id) {
		try {
			List<String> fotos = this.service.getFotosUsuario(id);
			if (fotos == null)
				return new ResponseEntity<String>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<List<String>>(fotos, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
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

	@PutMapping("/nombre")
	public ResponseEntity<?> changeNombreUsuario(@RequestBody Usuario usuario) {
		try {
			Integer result = this.service.changeNombreUsuario(usuario);
			if (result == 0)
				return new ResponseEntity<String>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<String>("Nombre cambiado con éxito!!!", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/fotos")
	public ResponseEntity<?> changeFotosUsuario(@RequestBody Usuario usuario) {
		try {
			Integer resultado = this.service.changeFotosUsuario(usuario);
			if (resultado == 0)
				return new ResponseEntity<String>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<String>("Fotos cambiadas con éxito!!!", HttpStatus.OK);
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
}
