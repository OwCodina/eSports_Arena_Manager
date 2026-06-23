# ── Etapa 1: BUILD ─────────────────────────────────────────────────────────────
# Context = raíz del proyecto (donde está el pom.xml padre).
# ARG MODULE_NAME recibe el nombre del módulo desde docker-compose.
FROM maven:3.9-eclipse-temurin-21 AS build

ARG MODULE_NAME
WORKDIR /app

# Copiar el pom.xml raíz primero (resuelve el <parent> de cada módulo)
COPY pom.xml .

# Copiar todos los módulos (Maven necesita ver la estructura multi-módulo)
COPY msvc-eureka/pom.xml       ./msvc-eureka/pom.xml
COPY msvc-gateway/pom.xml      ./msvc-gateway/pom.xml
COPY msvc-auth/pom.xml         ./msvc-auth/pom.xml
COPY msvc-game/pom.xml         ./msvc-game/pom.xml
COPY msvc-user/pom.xml         ./msvc-user/pom.xml
COPY msvc-tournament/pom.xml   ./msvc-tournament/pom.xml
COPY msvc-sanction/pom.xml     ./msvc-sanction/pom.xml
COPY msvc-team/pom.xml         ./msvc-team/pom.xml
COPY msvc-registration/pom.xml ./msvc-registration/pom.xml
COPY msvc-match/pom.xml        ./msvc-match/pom.xml
COPY msvc-result/pom.xml       ./msvc-result/pom.xml
COPY msvc-ranking/pom.xml      ./msvc-ranking/pom.xml
COPY msvc-prize/pom.xml        ./msvc-prize/pom.xml
COPY msvc-notification/pom.xml ./msvc-notification/pom.xml

# Descargar dependencias usando la estructura completa (mejor caché)
RUN mvn dependency:go-offline -pl ${MODULE_NAME} -am -B -q 2>/dev/null || true

# Copiar solo el código fuente del módulo a compilar
COPY ${MODULE_NAME}/src ./${MODULE_NAME}/src

# Compilar solo ese módulo y sus dependencias padre (-am = also make)
RUN mvn package -pl ${MODULE_NAME} -am -DskipTests -B -q

# ── Etapa 2: RUNTIME ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

ARG MODULE_NAME
WORKDIR /app

# El JAR se genera en: msvc-game/target/msvc-game-0.0.1-SNAPSHOT.jar
COPY --from=build /app/${MODULE_NAME}/target/*.jar app.jar

ENTRYPOINT ["java", \
  "-Xmx256m", \
  "-Xms128m", \
  "-XX:+UseContainerSupport", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
