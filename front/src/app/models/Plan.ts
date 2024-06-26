import { Curso } from './Curso';

export class Plan {
	private id_plan: Number;

	private nombre_plan: String;

	private precio_plan: Number;

	private cursos_plan: Curso[];

	constructor(_id_plan: Number, _nombre_plan: String, _precio_plan: Number, _cursos_plan: Curso[]) {
		this.id_plan = _id_plan;
		this.nombre_plan = _nombre_plan;
		this.precio_plan = _precio_plan;
		this.cursos_plan = _cursos_plan;
	}
}
