# Imagen base de Node.js 22.14 slim
FROM node:22.14-slim AS build

WORKDIR /app

# Copiar todo el código al contenedor
COPY . .

# Instalar las dependencias
RUN npm install

# Compila Angular con SSR (sin ejecutar el script de creación de env.ts aquí)
RUN npm run build:ssr

# Servir la aplicación utilizando un servidor Node.js
FROM node:22.15-slim AS production

WORKDIR /app

# Copiar la aplicación compilada desde la etapa de construcción
COPY --from=build /app/dist /app/dist

# Copiar el script de inicio y el script de creación de env.ts al contenedor
COPY /init.sh /init.sh

# Dar permisos de ejecución al script
RUN chmod +x /init.sh

# Exponer el puerto 4000 para SSR
EXPOSE 4000

# Configurar el comando de inicio para SSR
ENTRYPOINT ["/init.sh"]
