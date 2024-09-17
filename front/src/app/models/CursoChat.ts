import { ClaseChat } from './ClaseChat';
import { MensajeChat } from './MensajeChat';

export class CursoChat {
	public id: string;

	public idCurso: number;

	public clases: ClaseChat[];

	public mensajes: MensajeChat[];

	constructor(_id: string, _idCurso: number, _clases: ClaseChat[], _mensajes: MensajeChat[]) {
		this.id = _id;
		this.idCurso = _idCurso;
		this.clases = _clases;
		this.mensajes = _mensajes;
	}
}
