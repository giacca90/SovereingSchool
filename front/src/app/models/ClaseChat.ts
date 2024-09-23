import { MensajeChat } from './MensajeChat';

export class ClaseChat {
	public idClase: number;

	public idCurso: number;

	public nombre_clase: string;

	public mensajes: MensajeChat[];

	constructor(_idClase: number, _idCurso: number, _nombre_clase: string, _mensajes: MensajeChat[]) {
		this.idClase = _idClase;
		this.idCurso = _idCurso;
		this.nombre_clase = _nombre_clase;
		this.mensajes = _mensajes;
	}
}
