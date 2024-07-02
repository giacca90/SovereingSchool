import { Curso } from './Curso';

export class Plan {
	public id_plan: Number;

	public nombre_plan: String;

	public precio_plan: Number;

	public cursos_plan: Curso[];

	constructor(_id_plan: Number, _nombre_plan: String, _precio_plan: Number, _cursos_plan: Curso[]) {
		this.id_plan = _id_plan;
		this.nombre_plan = _nombre_plan;
		this.precio_plan = _precio_plan;
		this.cursos_plan = _cursos_plan;
	}
}
