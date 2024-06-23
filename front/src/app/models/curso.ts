export class Curso {
	public id_curso: number;

	public nombre_curso: string;

	public profesores_curso: number[];

	public fecha_publicacion_curso: Date;

	public clases_curso: number[];

	public planes_curso: number[];

	public precio_curso: number;

	constructor(_id_curso: number, _nombre_curso: string, _profesores_curso: number[], _fecha_publicacion_curso: Date, _clases_curso: number[], _planes_curso: number[], _precio_curso: number) {
		this.id_curso = _id_curso;
		this.nombre_curso = _nombre_curso;
		this.profesores_curso = _profesores_curso;
		this.fecha_publicacion_curso = _fecha_publicacion_curso;
		this.clases_curso = _clases_curso;
		this.planes_curso = _planes_curso;
		this.precio_curso = _precio_curso;
	}
}
