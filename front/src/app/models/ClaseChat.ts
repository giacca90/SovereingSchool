import { MensajeChat } from './MensajeChat';

export class ClaseChat {
	public idClase: number;

	public idCurso: number;

	public mensajes: MensajeChat[];

	constructor(_idClase: number, _idCurso: number, _mensajes: MensajeChat[]) {
		this.idClase = _idClase;
		this.idCurso = _idCurso;
		this.mensajes = _mensajes;
	}
}
