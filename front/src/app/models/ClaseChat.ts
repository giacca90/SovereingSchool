import { MensajeChat } from './MensajeChat';

export class ClaseChat {
	public id_clase: number;

	public id_curso: number;

	public nombre_clase: string;

	public mensajes: MensajeChat[];

	constructor(_id_clase: number, _id_curso: number, _nombre_clase: string, _mensajes: MensajeChat[]) {
		this.id_clase = _id_clase;
		this.id_curso = _id_curso;
		this.nombre_clase = _nombre_clase;
		this.mensajes = _mensajes;
	}
}
