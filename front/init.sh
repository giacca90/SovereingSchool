#!/bin/bash

# Crear el archivo keystore.p12 a partir de la variable de entorno KEYSTORE_CONTENT
if [ -z "$KEYSTORE_CONTENT" ]; then
  echo "Error: La variable de entorno KEYSTORE_CONTENT no está configurada."
  exit 1
fi

echo "$KEYSTORE_CONTENT" | base64 -d > /keystore.p12

# Verificar que el archivo keystore.p12 se haya creado correctamente
if [ -f /keystore.p12 ]; then
    echo "Keystore file created successfully."
else
    echo "Failed to create keystore file."
    exit 1
fi

# Arrancar la aplicación (ajustar el comando según cómo inicies tu app)
echo "Iniciando la aplicación SSR..."
exec node dist/server/main.js
