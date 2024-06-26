import { Curso } from './Curso';
import { Plan } from './Plan';

export class NuevoUsuario {
	public nombre_usuario: String | null = null;

	public correo_electronico: String | null = null;

	public password: String | null = null;

	public foto_usuario: String[] | null = null;

	public plan_usuario: Plan | null = null;

	public cursos_usuario: Curso[] | null = null;

	public fecha_registro_usuario: Date | null = null;

	constructor() {}
}
