import { CursoChat } from './CursoChat';
import { MensajeChat } from './MensajeChat';

export class InitChatUsuario {
	public idUsuario: number;
	public mensajes: MensajeChat[];
	public cursos: CursoChat[];

	constructor(_idUsuario: number, _chats: MensajeChat[], _cursos: CursoChat[]) {
		this.idUsuario = _idUsuario;
		this.mensajes = _chats;
		this.cursos = _cursos;
	}
}
