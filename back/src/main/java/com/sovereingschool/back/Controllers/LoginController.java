package com.sovereingschool.back.Controllers;

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

import com.sovereingschool.back.Interfaces.ILoginService;
import com.sovereingschool.back.Models.ChangePassword;
import com.sovereingschool.back.Models.Login;

@RestController
@RequestMapping("/login")
public class LoginController {

	@Autowired
	private ILoginService service;

	@GetMapping("/correo/{id}")
	public ResponseEntity<?> getCorreoLogin(@PathVariable Long id) {
		try {
			return new ResponseEntity<String>(this.service.getCorreoLogin(id), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/password/{id}")
	public ResponseEntity<?> getPasswordLogin(@PathVariable Long id) {
		try {
			return new ResponseEntity<String>(this.service.getPasswordLogin(id), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// TODO probar despues de arreglar en create
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteLogin(@PathVariable Long id) {
		try {
			return new ResponseEntity<String>(this.service.deleteLogin(id), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// TODO revisar porque no funciona
	@PostMapping("/new")
	public ResponseEntity<?> createNuevoLogin(@RequestBody Login login) {
		try {
			return new ResponseEntity<>(this.service.createNuevoLogin(login), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/cambiacorreo")
	public ResponseEntity<?> changeCorreoLogin(@RequestBody Login login) {
		try {
			return new ResponseEntity<>(this.service.changeCorreoLogin(login), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/cambiapassword")
	public ResponseEntity<?> changePasswordLogin(@RequestBody ChangePassword changepassword) {
		try {
			return new ResponseEntity<>(this.service.changePasswordLogin(changepassword), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
