export class Usuario {
	private _id_usuario: number;

	private _nombre_usuario: string;

	private _foto_usuario: string[];

	private _roll_usuario: number;

	private _plan_usuario: number;

	private _cursos_usuario: number[];

	private _fecha_registro_usuario: Date;

	constructor(id_usuario: number, nombre_usuario: string, foto_usuario: string[], roll_usuario: number, plan_usuario: number, cursos_usuario: number[], fecha_registro_usuario: Date) {
		this._id_usuario = id_usuario;
		this._nombre_usuario = nombre_usuario;
		this._foto_usuario = foto_usuario;
		this._roll_usuario = roll_usuario;
		this._plan_usuario = plan_usuario;
		this._cursos_usuario = cursos_usuario;
		this._fecha_registro_usuario = fecha_registro_usuario;
	}
}
