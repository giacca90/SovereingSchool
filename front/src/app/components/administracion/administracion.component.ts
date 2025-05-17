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
		public cursosService: CursosService,
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
			this.cursosService.getAllCursos().subscribe((data: Curso[] | null) => {
				if (data) {
					this.cursos = data;
					this.cursosSel = data;
				}
			});
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
			this.usuariosSel = this.usuarios.filter((u) => u.nombre_usuario.toLowerCase().includes(value.toLowerCase()) || u.roll_usuario?.toLowerCase().includes(value.toLowerCase()) || u.id_usuario.toString().includes(value));
		}
	}

	buscaCursos($event: Event) {
		const value: string = ($event.target as HTMLInputElement).value;
		if (value.length === 0) {
			this.cursosSel = this.cursos;
		} else {
			this.cursosSel = this.cursos.filter((c) => c.nombre_curso.toLowerCase().includes(value.toLowerCase()) || c.profesores_curso.toString().toLowerCase().includes(value.toLowerCase()) || c.id_curso.toString().includes(value));
		}
	}

	buscaChats($event: Event) {
		const value: string = ($event.target as HTMLInputElement).value;
		if (value.length === 0) {
			this.chatsSel = this.chats;
		} else {
			this.chatsSel = this.chats.filter((c) => c.nombre_curso.toLowerCase().includes(value.toLowerCase()) || c.id_curso.toString().includes(value));
		}
	}

	eliminaUsuario(usuario: Usuario) {
		if (!confirm('¿Estás seguro que deseas eliminar este usuario?')) {
			return;
		}
		this.usuariosService.eliminaUsuario(usuario).subscribe((data: boolean) => {
			if (data) {
				this.usuariosService.getAllUsuarios().subscribe((data: Usuario[] | null) => {
					if (data) {
						this.usuarios = data;
						this.usuariosSel = data;
					}
				});
			}
		});
	}

	eliminaCurso(curso: Curso) {
		if (!confirm('¿Estás seguro que deseas eliminar este curso?\n Esto eliminará también el chat de este curso')) {
			return;
		}
		/* this.cursosService.deleteCurso(curso).subscribe((data: boolean) => {
			if (data) {
				this.cursosService.getAllCursos().subscribe((data: Curso[] | null) => {
					if (data) {
						this.cursos = data;
						this.cursosSel = data;
					}
				});
			}
		}); */
	}

	eliminaChat(chat: CursoChat) {
		if (!confirm('¿Estás seguro que deseas eliminar este chat?\n Esto no eliminará el curso de este chat')) {
			return;
		}
		/* this.chatsService.deleteChat(chat).subscribe((data: boolean) => {
			if (data) {
				this.chatsService.getAllChats().subscribe((data: CursoChat[] | null) => {
					if (data) {
						this.chats = data;
						this.chatsSel = data;
					}
				});
			}
		}); */
	}

	mostrarUsuario(idUsuario: number) {
		document.querySelectorAll('[id^="user-data-"]').forEach((d) => {
			if (d.id === `user-data-${idUsuario}`) {
				(d as HTMLDivElement).style.display = (d as HTMLDivElement).style.display === 'block' ? 'none' : 'block';
			} else {
				(d as HTMLDivElement).style.display = 'none';
			}
		});
	}

	mostrarCurso(idCurso: number) {
		document.querySelectorAll('[id^="curso-data-"]').forEach((d) => {
			if (d.id === `curso-data-${idCurso}`) {
				(d as HTMLDivElement).style.display = (d as HTMLDivElement).style.display === 'block' ? 'none' : 'block';
			} else {
				(d as HTMLDivElement).style.display = 'none';
			}
		});
	}
	mostrarChat(idCurso: number) {
		document.querySelectorAll('[id^="chat-data-"]').forEach((d) => {
			if (d.id === `chat-data-${idCurso}`) {
				(d as HTMLDivElement).style.display = (d as HTMLDivElement).style.display === 'block' ? 'none' : 'block';
			} else {
				(d as HTMLDivElement).style.display = 'none';
			}
		});
	}

	mostrarClaseChat(idClase: number) {
		document.querySelectorAll('[id^="clase-chat-"]').forEach((d) => {
			if (d.id === `clase-chat-${idClase}`) {
				(d as HTMLDivElement).style.display = (d as HTMLDivElement).style.display === 'block' ? 'none' : 'block';
			} else {
				(d as HTMLDivElement).style.display = 'none';
			}
		});
	}
}
