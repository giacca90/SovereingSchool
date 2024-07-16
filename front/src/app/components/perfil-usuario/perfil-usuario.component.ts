import { Component } from '@angular/core';
import { Usuario } from '../../models/Usuario';
import { LoginService } from '../../services/login.service';

@Component({
	selector: 'app-perfil-usuario',
	standalone: true,
	imports: [],
	templateUrl: './perfil-usuario.component.html',
	styleUrl: './perfil-usuario.component.css',
})
export class PerfilUsuarioComponent {
	editable: boolean = false;
	usuario: Usuario | null = null;
	constructor(private loginService: LoginService) {
		this.usuario = JSON.parse(JSON.stringify(this.loginService.usuario));
	}
	cargaFoto() {}
}
