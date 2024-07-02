INSERT INTO curso (
		fecha_publicacion_curso,
		nombre_curso,
		descriccion_corta,
		descriccion_larga,
		imagen_curso,
		precio_curso
	)
VALUES (
		'2024-01-01 00:00:00',
		'Curso de la REML',
		'Aprende a utilizar la R.E.M.L.,
la acreditación que te reconoce como el Soberano que eres.',
		'Curso completo sobre la Republica Errante del Menda Lerenda,
descubre sus fundamentos juridicos, sus potencialidades, como utilizarla con el D.N.I. o como unica acreditación.
En este curso hay todo lo que necesitas saber!!!',
		'/home/giacca90/Escritorio/Proyectos/SovereingSchool/front/src/assets/Logo-REML.png',
		50.00
	),
	(
		'2024-04-07 00:00:00',
		'Curso S.P.C.',
		'El mejor remedio al Fraude del nombre legal. Recupera el control sobre tus propiedades,
embargando al D.N.I.',
		'En este curso aprenderas como convertirte en Acreedor de parte asegurada, embargand en D.N.I. y convirtiendote en su acreedor.
		Ademas aprenderás como transferir tus activos a un Trust (fideicomiso) para protegerlas y poderla transferir a tus descendientes sin pagar impuestos.',
		'/home/giacca90/Escritorio/Proyectos/SovereingSchool/front/src/assets/sello-ex-libris-1.png',
		150.00
	),
	(
		'2024-08-07 00:00:00',
		'Curso de Libertad Financiera',
		'Aprende las técnicas de ingeniería financiera para dejar de pagar impuestos sobre tu trabajo !!',
		'Cansado de trabajar duro y ganar poco?? Estas arto de pagar impuestos sobre tu trabajo???
		Este curso es lo que buscas!!!
		Aprenderás las mejores tecnicas de ingeniería financiera, como los Fideicomisos, las empresas off-shore y fundaciónes.',
		'/home/giacca90/Escritorio/Proyectos/SovereingSchool/front/src/assets/roble texto.png',
		70.00
	),
	(
		'2024-08-18 00:00:00',
		'Curso de Copyright',
		'Desde la base hasta la practica. Registra tu nombre, tu casa, tu coche y todas tus obras como Marcas Registradas y obtiene el control absoluto.',
		'El derecho de Autor es el principal de los derechos, es el principio por lo cual el Vaticano se declaró como el propietario de toda la creación.
		Como ellos, nosotros tambien podemos utilizarlo como defensa contra los humanistas seculares.',
		'/home/giacca90/Escritorio/Proyectos/SovereingSchool/front/src/assets/Logo LJS morado.png',
		40.00
	);
INSERT INTO clase (
		direccion_clase,
		nombre_clase,
		descriccion_clase,
		posicion_clase,
		tipo_clase,
		id_curso
	)
VALUES (
		'http://corso1/clase1',
		'Clase 1 - Presentación',
		'Una breve presentación del profesor',
		1,
		1,
		1
	),
	(
		'http://corso1/clase2',
		'Clase 2 - Introdución',
		'Introducción al curso',
		2,
		1,
		1
	),
	(
		'http://corso1/clase3',
		'Clase 3 - Que es la REML???',
		'Que es la REML y como se creó??',
		3,
		1,
		1
	),
	(
		'http://corso1/clase4',
		'Clase 4 - Fundamentos legales',
		'El convenio de Montevideo y otros',
		4,
		1,
		1
	),
	(
		'http://corso2/clase1',
		'Clase 1 - Que es un Trust??',
		'Entendemos el Fideicomiso',
		1,
		1,
		2
	),
	(
		'http://corso2/clase2',
		'Clase 2 - Partes del Trust',
		'Fideicomitente, Fideicomisario y Beneficiario',
		2,
		1,
		2
	),
	(
		'http://corso3/clase1',
		'Clase 1 - Presentación',
		'Presentación del profesor',
		1,
		1,
		3
	),
	(
		'http://corso3/clase2',
		'Clase 2 - Las estructuras societarias',
		'Muchas es mejor!!',
		2,
		1,
		3
	),
	(
		'http://corso4/clase1',
		'Clase 1 - Tu nombre te define',
		'Tu nombre, tu marca',
		1,
		1,
		4
	),
	(
		'http://corso4/clase2',
		'Clase 2 - Copyright en Derecho Consuetudinario',
		'En common law se hace publico',
		2,
		1,
		4
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
		ARRAY ['/home/giacca90/Escritorio/Proyectos/SovereingSchool/front/src/assets/roble texto.png'],
		'Matt I de la Gole®',
		0,
		NULL
	),
	(
		'2024-01-01 00:00:00',
		ARRAY ['/home/giacca90/Escritorio/Proyectos/SovereingSchool/front/src/assets/sello-ex-libris-1.png'],
		'Ra Amon de la REML®',
		1,
		NULL
	),
	(
		'2024-01-01 00:00:00',
		ARRAY ['/home/giacca90/Escritorio/Proyectos/SovereingSchool/front/src/assets/Logo-REML.png'],
		'Rodri KMTR',
		1,
		NULL
	),
	(
		'2024-01-01 00:00:00',
		ARRAY ['/home/giacca90/Escritorio/Proyectos/SovereingSchool/front/src/assets/Logo LJS morado.png'],
		'El Equipo De L.J.S.',
		1,
		NULL
	),
	(
		'2024-01-01 00:00:00',
		ARRAY ['/home/giacca90/Escritorio/Proyectos/SovereingSchool/front/src/assets/jaime.jpg'],
		'Jaime',
		2,
		1
	);
INSERT INTO login (id_usuario, correo_electronico, PASSWORD)
VALUES (1, 'matt@prueba.com', 123456),
	(2, 'ra@prueba.com', 234567),
	(3, 'rodri@prueba.com', 987654),
	(4, 'equipoljs@prueba.com', 876543),
	(5, 'jaime@prueba.com', 345678);
INSERT INTO curso_profesor (id_curso, id_usuario)
VALUES (1, 3),
	(2, 2),
	(3, 1),
	(4, 4);
INSERT INTO usuario_curso (id_usuario, id_curso)
VALUES (3, 1),
	(3, 2);
INSERT INTO cursos_plan (id_curso, id_plan)
VALUES (1, 2),
	(2, 1);