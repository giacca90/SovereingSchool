import { CursoChat } from './CursoChat';
import { MensajeChat } from './MensajeChat';

export class InitChatUsuario {
	public idUsuario: number;
	public chats: MensajeChat[];
	public cursos: CursoChat[];

	constructor(_idUsuario: number, _chats: MensajeChat[], _cursos: CursoChat[]) {
		this.idUsuario = _idUsuario;
		this.chats = _chats;
		this.cursos = _cursos;
	}
}
