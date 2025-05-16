import { Component } from '@angular/core';
import { Curso } from '../../models/Curso';
import { CursoChat } from '../../models/CursoChat';
import { Usuario } from '../../models/Usuario';
import { ChatService } from '../../services/chat.service';
import { CursosService } from '../../services/cursos.service';
import { InitService } from '../../services/init.service';
import { UsuariosService } from '../../services/usuarios.service';

@Component({
	selector: 'app-administracion',
	imports: [],
	templateUrl: './administracion.component.html',
	styleUrl: './administracion.component.css',
})
export class AdministracionComponent {
	tipo: number = 0; // 0: Vacío  1: Usuarios  2: Cursos  3: Chats
	usuarios: Usuario[] = [];
	usuariosSel: Usuario[] = [];

	cursos: Curso[] = [];
	cursosSel: Curso[] = [];

	chats: CursoChat[] = [];
	chatsSel: CursoChat[] = [];

	constructor(
		private usuariosService: UsuariosService,
		private cursosService: CursosService,
		private chatsService: ChatService,
		private initService: InitService,
	) {}

	cargaUsuarios() {
		document.querySelectorAll('button').forEach((b) => b.classList.remove('text-green-700'));
		document.querySelector('#usuariosButton')?.classList.add('text-green-700');
		if (this.usuarios.length === 0) {
			this.usuariosService.getAllUsuarios().subscribe((data: Usuario[] | null) => {
				if (data) {
					this.usuarios = data;
					this.usuariosSel = data;
				}
			});
		}
		this.tipo = 1;
	}

	cargaCursos() {
		document.querySelectorAll('button').forEach((b) => b.classList.remove('text-green-700'));
		document.querySelector('#cursosButton')?.classList.add('text-green-700');
		if (this.cursos.length === 0) {
			if (this.cursosService.cursos.length === 0) {
				this.initService.carga().then(() => {
					this.cursos = this.cursosService.cursos;
					this.cursosSel = this.cursosService.cursos;
				});
			} else {
				this.cursos = this.cursosService.cursos;
				this.cursosSel = this.cursosService.cursos;
			}
		}
		this.tipo = 2;
	}

	cargaChats() {
		document.querySelectorAll('button').forEach((b) => b.classList.remove('text-green-700'));
		document.querySelector('#chatsButton')?.classList.add('text-green-700');
		if (this.chats.length === 0) {
			this.chatsService.getAllChats().subscribe((data: CursoChat[] | null) => {
				if (data) {
					this.chats = data;
					this.chatsSel = data;
				}
			});
		}
		this.tipo = 3;
	}

	buscaUsuarios($event: Event) {
		const value: string = ($event.target as HTMLInputElement).value;
		if (value.length === 0) {
			this.usuariosSel = this.usuarios;
		} else {
			this.usuariosSel = this.usuarios.filter((u) => u.nombre_usuario.toLowerCase().includes(value.toLowerCase()));
		}
	}
	buscaCursos($event: Event) {
		const value: string = ($event.target as HTMLInputElement).value;
		if (value.length === 0) {
			this.cursosSel = this.cursos;
		} else {
			this.cursosSel = this.cursos.filter((c) => c.nombre_curso.toLowerCase().includes(value.toLowerCase()));
		}
	}
	buscaChats($event: Event) {
		const value: string = ($event.target as HTMLInputElement).value;
		if (value.length === 0) {
			this.chatsSel = this.chats;
		} else {
			this.chatsSel = this.chats.filter((c) => c.nombre_curso.toLowerCase().includes(value.toLowerCase()));
		}
	}
}
