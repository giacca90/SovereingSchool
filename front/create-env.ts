import * as fs from 'fs';
import * as path from 'path';

const envPath = path.join(__dirname, '../src/environments.ts');

const content = `export class Env {
  public static readonly BACK_BASE: string = '${process.env['BACK_BASE'] ?? 'https://localhost:8080'}';
  public static readonly BACK_STREAM: string = '${process.env['BACK_STREAM'] ?? 'https://localhost:8090'}';
  public static readonly BACK_CHAT: string = '${process.env['BACK_CHAT'] ?? 'https://localhost:8070'}';
  public static readonly BACK_CHAT_WSS: string = '${process.env['BACK_CHAT_WSS'] ?? 'wss://localhost:8070'}';
  public static readonly BACK_STREAM_WSS: string = '${process.env['BACK_STREAM_WSS'] ?? 'wss://localhost:8090'}';
}
`;

fs.writeFileSync(envPath, content, 'utf8');
console.log('✅ Archivo env.ts generado dinámicamente.');
