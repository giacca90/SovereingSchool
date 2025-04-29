# Imagen base de Node.js 22.14 slim
FROM node:22.14-slim AS build

# Directorio de trabajo en el contenedor
WORKDIR /app

# Copiar package.json y package-lock.json para instalar dependencias primero
COPY package*.json ./

# Instalar dependencias de Angular SSR
RUN npm install --legacy-peer-deps

# Copiar el resto del código de la aplicación
COPY . .

# Construir la aplicación Angular SSR
RUN npm run build:ssr

# Servir la aplicación utilizando un servidor Node.js
FROM node:22.14-slim AS production

# Crear un directorio de trabajo en la imagen de producción
WORKDIR /app

# Copiar los archivos generados en el contenedor build
COPY --from=build /app/dist /app/dist

# Instalar las dependencias de producción
COPY package*.json ./
RUN npm install --only=production --legacy-peer-deps

# Exponer el puerto por defecto
EXPOSE 4000

# Comando para ejecutar la app SSR
CMD ["npm", "run", "serve:ssr"]
