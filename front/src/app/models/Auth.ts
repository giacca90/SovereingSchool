import { Usuario } from './Usuario';

export class Auth {
	public status: Boolean;
	public message: String;
	public usuario: Usuario;
	public accessToken: string;

	constructor(status: Boolean, message: string, usuario: Usuario, accessToken: string) {
		this.status = status;
		this.message = message;
		this.usuario = usuario;
		this.accessToken = accessToken;
	}
}
