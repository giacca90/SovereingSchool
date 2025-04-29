import { APP_BASE_HREF } from '@angular/common';
import { CommonEngine } from '@angular/ssr/node';
import express from 'express';
import fs from 'fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import bootstrap from './src/main.server';

// Genera el archivo `env.json` antes de iniciar el servidor
const envConfig = {
	BACK_BASE: process.env['BACK_BASE'],
	BACK_STREAM: process.env['BACK_STREAM'],
	BACK_CHAT: process.env['BACK_CHAT'],
	BACK_CHAT_WSS: process.env['BACK_CHAT_WSS'],
	BACK_STREAM_WWS: process.env['BACK_STREAM_WWS'],
};

// Directorio donde se guardará el archivo env.json
const serverDistFolder = dirname(fileURLToPath(import.meta.url));
const browserDistFolder = resolve(serverDistFolder, '../browser');
const envFilePath = join(browserDistFolder, 'assets/env.json');

// Asegúrate de que la carpeta existe
fs.mkdirSync(join(browserDistFolder, 'assets'), { recursive: true });

// Escribe el archivo env.json
fs.writeFileSync(envFilePath, JSON.stringify(envConfig, null, 2));
console.log('✅ Archivo env.json generado en', envFilePath);

export function app(): express.Express {
	const server = express();
	const indexHtml = join(serverDistFolder, 'index.server.html');

	const commonEngine = new CommonEngine();

	server.set('view engine', 'html');
	server.set('views', browserDistFolder);

	// Servir archivos estáticos, incluido el env.json
	server.get(
		'*/assets/*',
		express.static(browserDistFolder, {
			maxAge: '1y',
			setHeaders: (res) => {
				res.setHeader('Cache-Control', 'public, max-age=15768000');
			},
		}),
	);

	// Todas las rutas regulares usan el motor Angular
	server.get('*', (req, res, next) => {
		const { protocol, originalUrl, baseUrl, headers } = req;

		commonEngine
			.render({
				bootstrap,
				documentFilePath: indexHtml,
				url: `${protocol}://${headers.host}${originalUrl}`,
				publicPath: browserDistFolder,
				providers: [{ provide: APP_BASE_HREF, useValue: baseUrl }],
			})
			.then((html) => res.send(html))
			.catch((err) => next(err));
	});

	return server;
}

function run(): void {
	const port = process.env['PORT'] || 4000;

	// Levantar el servidor Express
	const server = app();
	server.listen(port, () => {
		console.log(`Node Express server listening on http://localhost:${port}`);
	});
}

run();
