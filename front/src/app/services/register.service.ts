import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Env } from '../../environment';
import { Auth } from '../models/Auth';
import { NuevoUsuario } from '../models/NuevoUsuario';
import { LoginService } from './login.service';

@Injectable({
	providedIn: 'root',
})
export class RegisterService {
	private apiUrl = Env.BACK_BASE;
	constructor(
		private http: HttpClient,
		private loginService: LoginService,
	) {}

	async registrarNuevoUsuario(nuevoUsuario: NuevoUsuario): Promise<boolean> {
		return new Promise(async (resolve, reject) => {
			const sub = this.http.post<Auth>(`${this.apiUrl}/usuario/nuevo`, nuevoUsuario, { observe: 'response', responseType: 'text' as 'json' }).subscribe({
				next: (response: HttpResponse<Auth>) => {
					if (response.status === 200 && response.body) {
						this.loginService.usuario = response.body.usuario;
						localStorage.setItem('Token', response.body.accessToken);
						resolve(true);
						sub.unsubscribe();
					} else {
						resolve(false);
					}
				},
				error: (error: HttpErrorResponse) => {
					console.error('HTTP request failed:', error);
					reject(false);
					sub.unsubscribe();
				},
			});
		});
	}
}
