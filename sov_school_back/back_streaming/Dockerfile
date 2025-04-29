# Etapa 1: Build de la aplicación
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml ./

# Descargar dependencias de Maven sin construir
RUN mvn dependency:go-offline

# Copiar el código fuente
COPY src ./src

# Construir el JAR de la aplicación
RUN mvn clean package -DskipTests

# Etapa 2: Crear la imagen final con solo el JAR construido
FROM eclipse-temurin:21-jre-slim

# Establecer directorio de trabajo
WORKDIR /app

# Instalar FFmpeg en la imagen base
RUN apt-get update && \
    apt-get install -y \
    wget \
    ca-certificates \
    && wget https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-i686-static.tar.xz \
    && tar -xf ffmpeg-release-i686-static.tar.xz \
    && mv ffmpeg-*-static/ffmpeg /usr/local/bin/ \
    && mv ffmpeg-*-static/ffprop /usr/local/bin/ \
    && rm -rf ffmpeg-release-i686-static.tar.xz ffmpeg-*-static

# Copiar el archivo JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto en el que el servicio Spring Boot escuchará
EXPOSE 8080

# Ejecutar la aplicación Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
