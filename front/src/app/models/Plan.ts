export class Plan {
	public id_plan: number;

	public nombre_plan: string;

	public precio_plan: number;

	constructor(_id_plan: number, _nombre_plan: string, _precio_plan: number) {
		this.id_plan = _id_plan;
		this.nombre_plan = _nombre_plan;
		this.precio_plan = _precio_plan;
	}
}
