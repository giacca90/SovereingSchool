import { Curso } from './Curso';
import { Plan } from './Plan';

export class Usuario {
	public id_usuario: number;

	public nombre_usuario: string;

	public foto_usuario: string[];

	public presentacion: string;

	public roll_usuario?: number;

	public plan_usuario?: Plan;

	public cursos_usuario?: Curso[];

	public fecha_registro_usuario?: Date;

	constructor(_id_usuario: number, _nombre_usuario: string, _foto_usuario: string[], _presentacion: string, _roll_usuario?: number, _plan_usuario?: Plan, _cursos_usuario?: Curso[], _fecha_registro_usuario?: Date) {
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
