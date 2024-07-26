import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Usuario } from '../../models/Usuario';
import { LoginService } from '../../services/login.service';
import { UsuariosService } from '../../services/usuarios.service';

@Component({
	selector: 'app-perfil-usuario',
	standalone: true,
	imports: [FormsModule],
	templateUrl: './perfil-usuario.component.html',
	styleUrl: './perfil-usuario.component.css',
})
export class PerfilUsuarioComponent {
	editable: boolean = false;
	usuario: Usuario | null = null;
	fotos: FileList | null = null;

	url: string = 'http://localhost:8080/usuario/';
	constructor(
		private loginService: LoginService,
		private usuarioService: UsuariosService,
		private http: HttpClient,
	) {
		this.usuario = JSON.parse(JSON.stringify(this.loginService.usuario));
	}
	cargaFoto(event: Event) {
		const input = event.target as HTMLInputElement;
		if (!input.files) {
			return;
		}
		this.fotos = input.files;

		Array.from(this.fotos).forEach((file) => {
			const reader = new FileReader();
			reader.onload = (e: ProgressEvent<FileReader>) => {
				if (e.target) {
					this.usuario?.foto_usuario.push(e.target.result as string);
				}
			};
			reader.readAsDataURL(file);
		});
	}

	save() {
		if (JSON.stringify(this.usuario) !== JSON.stringify(this.loginService.usuario) || (document.getElementById('fotoPrincipal') as HTMLImageElement).src !== this.usuario?.foto_usuario[0]) {
			const formData = new FormData();
			if (this.fotos) {
				Array.from(this.fotos).forEach((file) => {
					formData.append('files', file, file.name);
				});
				this.usuarioService
					.save(formData)
					.then((response) => {
						if (this.usuario?.foto_usuario && response) {
							const temp: string[] = [];
							for (let i = 0; i < this.usuario.foto_usuario.length; i++) {
								if (this.loginService.usuario?.foto_usuario.includes(this.usuario.foto_usuario[i])) {
									temp.push(this.usuario.foto_usuario[i]);
								}
							}
							this.usuario.foto_usuario = temp;
							response.forEach((resp) => {
								this.usuario?.foto_usuario.push(resp);
							});
							this.actualizaUsuario();
							this.fotos = null;
						}
					})
					.catch((error) => {
						console.error('Error al subir las fotos: ' + error.message);
					});
			} else {
				this.actualizaUsuario();
			}
		}
	}

	actualizaUsuario() {
		const temp: Usuario = JSON.parse(JSON.stringify(this.loginService.usuario));
		if (this.usuario?.foto_usuario && this.loginService.usuario?.foto_usuario !== undefined) {
			const fotoPrincipal: string = (document.getElementById('fotoPrincipal') as HTMLImageElement).src;
			if (fotoPrincipal !== this.usuario.foto_usuario[0]) {
				const f: string[] = [];
				f.push(fotoPrincipal);
				this.usuario.foto_usuario.forEach((foto: string) => {
					if (foto !== fotoPrincipal) {
						f.push(foto);
					}
				});
				this.usuario.foto_usuario = f;
			}
			temp.foto_usuario = this.usuario.foto_usuario;
			temp.nombre_usuario = this.usuario?.nombre_usuario;
			temp.presentacion = this.usuario.presentacion;
			temp.cursos_usuario?.forEach((curso) => {
				curso.clases_curso = undefined;
				curso.planes_curso = undefined;
				curso.precio_curso = undefined;
				curso.profesores_curso.forEach((profe) => {
					profe.fecha_registro_usuario = undefined;
					profe.cursos_usuario = undefined;
					profe.plan_usuario = undefined;
					profe.roll_usuario = undefined;
					if (profe.id_usuario === this.usuario?.id_usuario) {
						profe.foto_usuario = temp.foto_usuario;
					}
				});
			});

			if (temp.plan_usuario?.nombre_plan) temp.plan_usuario.nombre_plan = undefined;
			if (temp.plan_usuario?.precio_plan) temp.plan_usuario.precio_plan = undefined;
			this.usuarioService
				.actializaUsuario(temp)
				.then(() => {
					localStorage.clear;
					localStorage.setItem('Usuario', JSON.stringify(this.usuario));
				})
				.catch((error: Error) => {
					console.error('Error en actualizar el usuario: ' + error.message);
				});
		}
	}

	cambiaFoto(index: number) {
		if (this.usuario?.foto_usuario) {
			(document.getElementById('fotoPrincipal') as HTMLImageElement).src = this.usuario.foto_usuario[index];
			if (this.editable) {
				for (let i = 0; i < this.usuario?.foto_usuario.length; i++) {
					document.getElementById('foto-' + i)?.classList.remove('border-black');
					document.getElementById('foto-' + i)?.classList.remove('border');
				}
				(document.getElementById('foto-' + index) as HTMLImageElement).classList.add('border');
				(document.getElementById('foto-' + index) as HTMLImageElement).classList.add('border-black');
			}
		}
	}
}
