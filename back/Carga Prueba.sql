INSERT INTO curso (
		fecha_publicacion_curso,
		nombre_curso,
		precio_curso
	)
VALUES ('2024-01-01 00:00:00', 'Curso de la REML', 50.00),
	('2024-04-07 00:00:00', 'Curso Trust', 150.00);
INSERT INTO clase (
		direccion_clase,
		nombre_clase,
		posicion_clase,
		tipo_clase,
		id_curso
	)
VALUES (
		'http://corso1/clase1',
		'Clase 1 - Presentación',
		1,
		1,
		1
	),
	(
		'http://corso1/clase2',
		'Clase 2 - Introdución',
		2,
		1,
		1
	),
	(
		'http://corso1/clase3',
		'Clase 3 - Que es la REML???',
		3,
		1,
		1
	),
	(
		'http://corso1/clase4',
		'Clase 4 - Fundamentos legales',
		4,
		1,
		1
	),
	(
		'http://corso2/clase1',
		'Clase 1 - Que es un Trust??',
		1,
		1,
		2
	),
	(
		'http://corso2/clase1',
		'Clase 2 - Partes del Trust',
		2,
		1,
		2
	);
INSERT INTO plan (nombre_plan, precio_plan)
VALUES ('Absolute', 1500.00),
	('Student', 100.00);
INSERT INTO usuario (
		fecha_registro_usuario,
		foto_usuario,
		nombre_usuario,
		roll_usuario,
		plan_usuario
	)
VALUES (
		'2024-01-01 00:00:00',
		ARRAY ['fotoMat1','fotoMat2'],
		'Matt I de la Gole®',
		0,
		NULL
	),
	(
		'2024-01-01 00:00:00',
		ARRAY ['fotoRa1','fotoRa2'],
		'Ra Amon de la REML®',
		1,
		NULL
	),
	(
		'2024-01-01 00:00:00',
		ARRAY ['fotoJaime','fotoJaime2'],
		'Jaime',
		2,
		1
	);
INSERT INTO login (id_usuario, correo_electronico, PASSWORD)
VALUES (1, 'matt@prueba', 123456),
	(2, 'ra@prueba', 234567),
	(3, 'jaime@prueba', 345678);
INSERT INTO curso_profesor (id_curso, id_usuario)
VALUES (1, 2),
	(2, 1);
INSERT INTO usuario_curso (id_usuario, id_curso)
VALUES (3, 1),
	(3, 2);
INSERT INTO cursos_plan (id_curso, id_plan)
VALUES (1, 2),
	(2, 1);