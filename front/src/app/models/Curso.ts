import { Usuario } from './Usuario';

export class Curso {
	public id_curso: number;

	public nombre_curso: string;

	public profesores_curso: Usuario[];

	public fecha_publicacion_curso: Date;

	public clases_curso: number[];

	public planes_curso: number[];

	public descriccion_corta: String;

	public descriccion_larga: String;

	public imagen_curso: String;

	public precio_curso: number;

	constructor(_id_curso: number, _nombre_curso: string, _profesores_curso: Usuario[], _fecha_publicacion_curso: Date, _clases_curso: number[], _planes_curso: number[], _descriccion_corta: String, _descriccion_larga: String, _imagen_curso: String, _precio_curso: number) {
		this.id_curso = _id_curso;
		this.nombre_curso = _nombre_curso;
		this.profesores_curso = _profesores_curso;
		this.fecha_publicacion_curso = _fecha_publicacion_curso;
		this.clases_curso = _clases_curso;
		this.planes_curso = _planes_curso;
		this.descriccion_corta = _descriccion_corta;
		this.descriccion_larga = _descriccion_larga;
		this.imagen_curso = _imagen_curso;
		this.precio_curso = _precio_curso;
	}
}
