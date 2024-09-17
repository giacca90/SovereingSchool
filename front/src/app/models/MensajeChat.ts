export class MensajeChat {
	public idMensaje: number;

	public idCurso: number;

	public idClase: number;

	public idUsuario: number;

	public mensaje: string;

	constructor(_idMensaje: number, _idCurso: number, _idClase: number, _idUsuario: number, _message: string) {
		this.idMensaje = _idMensaje;
		this.idCurso = _idCurso;
		this.idClase = _idClase;
		this.idUsuario = _idUsuario;
		this.mensaje = _message;
	}
}
