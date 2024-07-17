import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Usuario } from '../../models/Usuario';
import { LoginService } from '../../services/login.service';

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
		private http: HttpClient,
		private cdr: ChangeDetectorRef,
	) {
		this.usuario = JSON.parse(JSON.stringify(this.loginService.usuario));
	}
	cargaFoto(event: Event) {
		console.log('Llamado cargaFoto');
		const input = event.target as HTMLInputElement;
		if (!input.files) {
			console.log('input nulo');
			return;
		}
		this.fotos = input.files;
		console.log('Fotos: ' + this.fotos.length);

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
		if (JSON.stringify(this.usuario) !== JSON.stringify(this.loginService.usuario)) {
			console.log('Hay cambios!!!');

			const formData = new FormData();
			if (this.fotos) {
				Array.from(this.fotos).forEach((file) => {
					console.log('For: ' + file.name);
					formData.append('files', file, file.name);
				});

				this.http.post<string[]>(this.url + 'subeFotos', formData).subscribe({
					next: (response) => {
						if (this.usuario?.foto_usuario) {
							const temp: string[] = [];
							for (let i = 0; i < this.usuario.foto_usuario.length; i++) {
								if (this.loginService.usuario?.foto_usuario.includes(this.usuario.foto_usuario[i])) temp.push(this.usuario.foto_usuario[i]);
							}
							this.usuario.foto_usuario = temp;
							console.log('Response: ' + response);
							response.forEach((resp) => {
								this.usuario?.foto_usuario.push(resp);
							});
							this.actualizaUsuario();
						}
					},
					error: (error: Error) => {
						console.error('Error al subir las fotos: ' + error.message);
					},
				});
			} else {
				this.actualizaUsuario();
			}
		}
	}

	actualizaUsuario() {
		const temp: Usuario = JSON.parse(JSON.stringify(this.loginService.usuario));
		if (temp?.foto_usuario && this.usuario?.foto_usuario && this.loginService.usuario?.foto_usuario !== undefined) {
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
			console.log('Objecto a enviar: \n' + JSON.stringify(temp));
			const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
			this.http.put(this.url + 'edit', temp, { headers, responseType: 'text' }).subscribe({
				next: (resp) => {
					console.log(resp);
					localStorage.clear;
					localStorage.setItem('Usuario', JSON.stringify(this.usuario));
				},
				error: (e: Error) => {
					console.error('Error en actualizar el usuario: ' + e.message);
				},
			});
		}
	}
}
