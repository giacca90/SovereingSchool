export class Clase {
	private _id_clase: number;

	private _nombre_clase: string;

	private _tipo_clase: number;

	private _direccion_clase: string;

	private _posicion_clase: number;

	private _curso_clase: number;

	constructor(id_clase: number, nombre_clase: string, tipo_clase: number, direccion_clase: string, posicion_clase: number, curso_clase: number) {
		this._id_clase = id_clase;
		this._nombre_clase = nombre_clase;
		this._tipo_clase = tipo_clase;
		this._direccion_clase = direccion_clase;
		this._posicion_clase = posicion_clase;
		this._curso_clase = curso_clase;
	}
}
