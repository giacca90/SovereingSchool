#!/bin/bash

# Crear el archivo keystore.p12 a partir de la variable de entorno KEYSTORE_CONTENT
echo "$KEYSTORE_CONTENT" | base64 -d > /keystore.p12

# Verificar que el archivo se creó correctamente
if [ -f /keystore.p12 ]; then
    echo "Keystore file created successfully."
else
    echo "Failed to create keystore file."
    exit 1
fi

# Arrancar la aplicación (ajusta el comando según cómo inicies tu app)
exec java -jar /app/app.jar
