import { Curso } from './Curso';

export class Usuario {
	public id_usuario: Number;

	public nombre_usuario: String;

	public foto_usuario: String[];

	public presentacion: String;

	public roll_usuario: Number;

	public plan_usuario: Number;

	public cursos_usuario: Curso[];

	public fecha_registro_usuario: Date;

	constructor(_id_usuario: Number, _nombre_usuario: String, _foto_usuario: String[], _presentacion: String, _roll_usuario: Number, _plan_usuario: Number, _cursos_usuario: Curso[], _fecha_registro_usuario: Date) {
		this.id_usuario = _id_usuario;
		this.nombre_usuario = _nombre_usuario;
		this.foto_usuario = _foto_usuario;
		this.presentacion = _presentacion;
		this.roll_usuario = _roll_usuario;
		this.plan_usuario = _plan_usuario;
		this.cursos_usuario = _cursos_usuario;
		this.fecha_registro_usuario = _fecha_registro_usuario;
	}
}
