export class MensajeChat {
	id_mensaje: number;
	id_curso: number;
	id_clase: number;
	id_usuario: number;

	nombre_curso: string;
	nombre_clase: string;
	nombre_usuario: string;

	foto_curso: string;
	foto_usuario: string;

	mensaje: string;

	constructor(_id_mensaje: number, _id_curso: number, _id_clase: number, _id_usuario: number, _nombre_curso: string, _nombre_clase: string, _nombre_usuario: string, _foto_curso: string, _foto_usuario: string, _mensaje: string) {
		this.id_mensaje = _id_mensaje;
		this.id_curso = _id_curso;
		this.id_clase = _id_clase;
		this.id_usuario = _id_usuario;
		this.nombre_curso = _nombre_curso;
		this.nombre_clase = _nombre_clase;
		this.nombre_usuario = _nombre_usuario;
		this.foto_curso = _foto_curso;
		this.foto_usuario = _foto_usuario;
		this.mensaje = _mensaje;
	}
}
