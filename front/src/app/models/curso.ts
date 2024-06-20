export class Curso {
	private _id_curso: number;

	private _nombre_curso: string;

	private _profesores_curso: number[];

	private _fecha_publicacion_curso: Date;

	private _clases_curso: number[];

	private _planes_curso: number[];

	private _precio_curso: number;

	constructor(id_curso: number, nombre_curso: string, profesores_curso: number[], fecha_publicacion_curso: Date, clases_curso: number[], planes_curso: number[], precio_curso: number) {
		this._id_curso = id_curso;
		this._nombre_curso = nombre_curso;
		this._profesores_curso = profesores_curso;
		this._fecha_publicacion_curso = fecha_publicacion_curso;
		this._clases_curso = clases_curso;
		this._planes_curso = planes_curso;
		this._precio_curso = precio_curso;
	}
}
