export class MensajeChat {
	id_mensaje: string | null;
	id_curso: number | null;
	id_clase: number | null;
	id_usuario: number | undefined;

	nombre_curso: string | null;
	nombre_clase: string | null;
	nombre_usuario: string | null;

	foto_curso: string | null;
	foto_usuario: string | null;

	respuesta: MensajeChat | null;

	mensaje: string;

	fecha: Date;

	constructor(_id_mensaje: string | null, _id_curso: number | null, _id_clase: number | null, _id_usuario: number | undefined, _nombre_curso: string | null, _nombre_clase: string | null, _nombre_usuario: string | null, _foto_curso: string | null, _foto_usuario: string | null, _respuesta: MensajeChat | null, _mensaje: string, _fecha: Date) {
		this.id_mensaje = _id_mensaje;
		this.id_curso = _id_curso;
		this.id_clase = _id_clase;
		this.id_usuario = _id_usuario;
		this.nombre_curso = _nombre_curso;
		this.nombre_clase = _nombre_clase;
		this.nombre_usuario = _nombre_usuario;
		this.foto_curso = _foto_curso;
		this.foto_usuario = _foto_usuario;
		this.respuesta = _respuesta;
		this.mensaje = _mensaje;
		this.fecha = _fecha;
	}
}
