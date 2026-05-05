# Multi-stage: compila con Maven dentro del contenedor y produce una imagen ligera
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
# Copiar wrappers y descriptor primero para cachear dependencias
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline || true
# Ahora copiar el código y compilar (omitimos tests aquí; se corren con generar-reportes-jacoco)
COPY src src
RUN ./mvnw -B -q clean package -DskipTests

# Imagen final liviana
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
VOLUME /tmp
COPY --from=builder /app/target/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
