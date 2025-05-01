#!/bin/bash

# Verifica que las variables están definidas
if [ -z "$SSL_CERT_B64" ] || [ -z "$SSL_KEY_B64" ]; then
  echo "Error: Faltan variables de entorno SSL_CERT_B64 o SSL_KEY_B64"
  exit 1
fi

# Crea los archivos en /app
echo "$SSL_CERT_B64" | base64 -d > /app/cert.pem
echo "$SSL_KEY_B64" | base64 -d > /app/key.pem

# Verifica existencia
[ -f /app/cert.pem ] && echo "Certificado generado correctamente"
[ -f /app/key.pem ] && echo "Clave generada correctamente"

# Ejecuta tu app
exec node dist/front/server/server.mjs
