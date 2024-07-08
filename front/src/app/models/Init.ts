export class Init {
	public profesInit: ProfesInit[];
	public cursosInit: CursosInit[];

	constructor(_curso: CursosInit[], _prof: ProfesInit[]) {
		this.profesInit = _prof;
		this.cursosInit = _curso;
	}
}

export class ProfesInit {
	id_usuario: number;
	nombre_usuario: string;
	foto_usuario: string[];
	presentacion: string;

	constructor(_id: number, _nombre: string, _foto: string[], _presentacion: string) {
		this.id_usuario = _id;
		this.nombre_usuario = _nombre;
		this.foto_usuario = _foto;
		this.presentacion = _presentacion;
	}
}

export class CursosInit {
	id_curso: number;
	nombre_curso: string;
	profesores_curso: number[];
	descriccion_corta: string;
	imagen_curso: string;

	constructor(_id: number, _nombre: string, _prof: number[], _desc: string, _imagen: string) {
		this.id_curso = _id;
		this.nombre_curso = _nombre;
		this.profesores_curso = _prof;
		this.descriccion_corta = _desc;
		this.imagen_curso = _imagen;
	}
}
