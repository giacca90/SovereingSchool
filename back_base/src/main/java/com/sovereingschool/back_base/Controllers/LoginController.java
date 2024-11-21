package com.sovereingschool.back_base.Controllers;

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

import com.sovereingschool.back_base.DTOs.ChangePassword;
import com.sovereingschool.back_base.Interfaces.ILoginService;
import com.sovereingschool.back_base.Interfaces.IUsuarioService;
import com.sovereingschool.back_base.Models.Login;
import com.sovereingschool.back_base.Models.Usuario;

@RestController
@RequestMapping("/login")
public class LoginController {

	@Autowired
	private ILoginService service;

	@Autowired
	private IUsuarioService usuarioService;

	// Utilizado
	@GetMapping("/{correo}")
	public ResponseEntity<?> conpruebaCorreo(@PathVariable String correo) {
		Object response = new Object();
		try {
			response = this.service.compruebaCorreo(correo);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener el correo del usuario: " + e.getMessage();
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Utilizado

	@GetMapping("/{id}/{password}")
	public ResponseEntity<?> getUsuario(@PathVariable Long id, @PathVariable String password) {
		Object response = new Object();
		try {
			Login login = this.service.getLogin(id);
			if (login.getPassword().equals(password)) {
				Usuario usuario = this.usuarioService.getUsuario(id);
				response = usuario;
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				response = "Contraseña incorrecta";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			response = "Error en obtener la contraseña: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/correo/{id}")
	public ResponseEntity<?> getCorreoLogin(@PathVariable Long id) {
		Object response = new Object();
		try {
			String correo = this.service.getCorreoLogin(id);
			if (correo == null) {
				response = "Usuario no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = correo;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener el correo: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/password/{id}")
	public ResponseEntity<?> getPasswordLogin(@PathVariable Long id) {
		Object response = new Object();
		try {
			String password = this.service.getPasswordLogin(id);
			if (password == null) {
				response = "Usuario no encontrado";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			response = password;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en obtener la contraseña: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/new")
	public ResponseEntity<?> createNuevoLogin(@RequestBody Login login) {
		Object response = new Object();
		try {
			response = this.service.createNuevoLogin(login);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en crear el nuevo login " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/cambiaCorreo")
	public ResponseEntity<?> changeCorreoLogin(@RequestBody Login login) {
		Object response = new Object();
		try {
			if (login.getCorreo_electronico() == null) {
				response = "El correo electrónico no puede ser vació";
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			if (login.getCorreo_electronico().length() < 1) {
				response = "El correo electrónico no puede ser vació";
				return new ResponseEntity<>(response, HttpStatus.FAILED_DEPENDENCY);
			}
			response = this.service.changeCorreoLogin(login);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en cambiar de correo: " + e.getMessage();
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/cambiaPassword")
	public ResponseEntity<?> changePasswordLogin(@RequestBody ChangePassword changepassword) {
		Object response = new Object();
		try {
			Integer respuesta = this.service.changePasswordLogin(changepassword);
			if (respuesta == null) {
				response = "La contraseña no puede estar vacía";
				return new ResponseEntity<>(response, HttpStatus.FAILED_DEPENDENCY);
			}
			if (respuesta == 0) {
				response = "Las contraseñas no coinciden";
				return new ResponseEntity<>(response, HttpStatus.FAILED_DEPENDENCY);
			}
			response = "Contraseña cambiada con éxito!!!";
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en cambiar la contraseña: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteLogin(@PathVariable Long id) {
		Object response = new Object();
		try {
			response = this.service.deleteLogin(id);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response = "Error en cambiar la contraseña: " + e.getMessage();
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
