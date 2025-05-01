import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, of } from 'rxjs';
import { Usuario } from '../models/Usuario';

@Injectable({
	providedIn: 'root',
})
export class UsuariosService {
	public profes: Usuario[] = [];
	constructor(private http: HttpClient) {}

	get apiUrl(): string {
		if (typeof window !== 'undefined' && (window as any).__env) {
			const url = (window as any).__env.BACK_BASE ?? '';
			return url + '/usuario/';
		}
		return '';
	}

	getUsuario(id_usuario: number) {
		const sub = this.http.get<Usuario>(this.apiUrl + id_usuario, { observe: 'response' }).subscribe({
			next: (response: HttpResponse<Usuario>) => {
				if (response.ok && response.body) {
					sub.unsubscribe();
					return response.body;
				}
				return null;
			},
			error: (e: Error) => {
				sub.unsubscribe();
				console.error(e.message);
				return null;
			},
		});
	}

	getNombreProfe(id: number) {
		return this.profes.find((profe: Usuario) => profe.id_usuario === id)?.nombre_usuario.toString();
	}

	save(formData: FormData) {
		return this.http.post<string[]>(this.apiUrl + 'subeFotos', formData, { observe: 'response' }).pipe(
			map((response: HttpResponse<string[]>) => {
				if (response.status === 200) {
					return response.body;
				}
				return null;
			}),
			catchError((e: Error) => {
				console.error('Error en guardar las fotos: ' + e.message);
				return of(null);
			}),
		);
	}

	actualizaUsuario(temp: Usuario) {
		return this.http.put<string>(this.apiUrl + 'edit', temp, { observe: 'response', responseType: 'text' as 'json' }).pipe(
			map((response: HttpResponse<string>) => {
				if (response.status === 200) {
					return true;
				}
				return false;
			}),
			catchError((e: Error) => {
				console.error('Error en actualizar el usuario: ' + e.message);
				return of(false);
			}),
		);
	}
}
