export class Estadistica {
	profesores: number;
	alumnos: number;
	cursos: number;
	clases: number;

	constructor(_profesores: number, _alumnos: number, _cursos: number, _clases: number) {
		this.profesores = _profesores;
		this.alumnos = _alumnos;
		this.cursos = _cursos;
		this.clases = _clases;
	}
}
