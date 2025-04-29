# Imagen base de Node.js 22.14 slim
FROM node:22.14-slim AS build

WORKDIR /app

COPY . .

RUN npm install

# Ejecuta el script que genera env.ts
RUN npx ts-node create-env.ts

# Compila Angular con SSR
RUN npm run build:ssr

# Servir la aplicación utilizando un servidor Node.js
FROM node:22.14-slim AS production

WORKDIR /app

COPY --from=build /app/dist /app/dist

# Configura los comandos de ejecución SSR
CMD ["node", "dist/server/main.js"]
