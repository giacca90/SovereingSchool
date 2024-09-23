import { ClaseChat } from './ClaseChat';
import { MensajeChat } from './MensajeChat';

export class CursoChat {
	id_curso: number;

	clases: ClaseChat[];

	mensajes: MensajeChat[];

	nombre_curso: string;

	foto_curso: string;

	constructor(_id_curso: number, _clases: ClaseChat[], _mensajes: MensajeChat[], _nombre_curso: string, _foto_curso: string) {
		this.id_curso = _id_curso;
		this.clases = _clases;
		this.mensajes = _mensajes;
		this.nombre_curso = _nombre_curso;
		this.foto_curso = _foto_curso;
	}
}
