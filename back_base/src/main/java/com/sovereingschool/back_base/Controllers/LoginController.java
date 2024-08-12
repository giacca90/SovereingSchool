package com.sovereingschool.back_base.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(origins = "http://localhost:4200, https://giacca90.github.io")
@RequestMapping("/login")
public class LoginController {

	@Autowired
	private ILoginService service;

	@Autowired
	private IUsuarioService usuarioService;

	// Utilizado
	@GetMapping("/{correo}")
	public ResponseEntity<?> conpruebaCorreo(@PathVariable String correo) {
		try {
			return new ResponseEntity<Long>(this.service.compruebaCorreo(correo), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Utilizado

	@GetMapping("/{id}/{password}")
	public ResponseEntity<?> getUsuario(@PathVariable Long id, @PathVariable String password) {
		try {
			Login login = this.service.getLogin(id);
			if (login.getPassword().equals(password)) {
				Usuario usuario = this.usuarioService.getUsuario(id);
				return new ResponseEntity<Usuario>(usuario, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}

		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/correo/{id}")
	public ResponseEntity<?> getCorreoLogin(@PathVariable Long id) {
		try {
			String correo = this.service.getCorreoLogin(id);
			if (correo == null)
				return new ResponseEntity<String>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<String>(correo, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/password/{id}")
	public ResponseEntity<?> getPasswordLogin(@PathVariable Long id) {
		try {
			String password = this.service.getPasswordLogin(id);
			if (password == null)
				return new ResponseEntity<String>("Usuario no encontrado", HttpStatus.NOT_FOUND);
			return new ResponseEntity<String>(password, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/new")
	public ResponseEntity<?> createNuevoLogin(@RequestBody Login login) {
		try {
			return new ResponseEntity<String>(this.service.createNuevoLogin(login), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/cambiaCorreo")
	public ResponseEntity<?> changeCorreoLogin(@RequestBody Login login) {
		try {
			if (login.getCorreo_electronico() == null)
				return new ResponseEntity<>("El correo electrónico no puede ser vació", HttpStatus.NOT_FOUND);
			if (login.getCorreo_electronico().length() < 1)
				return new ResponseEntity<>("El correo electrónico no puede ser vació", HttpStatus.FAILED_DEPENDENCY);
			return new ResponseEntity<>(this.service.changeCorreoLogin(login), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/cambiaPassword")
	public ResponseEntity<?> changePasswordLogin(@RequestBody ChangePassword changepassword) {
		try {
			Integer respuesta = this.service.changePasswordLogin(changepassword);
			if (respuesta == null)
				return new ResponseEntity<String>("La contraseña no puede estar vacía", HttpStatus.FAILED_DEPENDENCY);
			if (respuesta == 0)
				return new ResponseEntity<>("Las contraseñas no coinciden", HttpStatus.FAILED_DEPENDENCY);
			return new ResponseEntity<>("Contraseña cambiada con éxito!!!", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteLogin(@PathVariable Long id) {
		try {
			return new ResponseEntity<String>(this.service.deleteLogin(id), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
