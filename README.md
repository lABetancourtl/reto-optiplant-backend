# reto-optiplant-backend

Backend de Optiplant desarrollado con Spring Boot.

## Stack
- Java 17
- Spring Boot
- Spring Security + JWT
- PostgreSQL
- Gradle
- Swagger / OpenAPI

## Estructura
Proyecto organizado por capas (`controller`, `service`, `repository`, `entity`, `dto`, `configuration/security`).

## Ejecucion local (sin Docker)

Requisitos:
- Java 17
- PostgreSQL corriendo en `localhost:5432`

Comando:

```bash
./gradlew bootRun
```

Variables opcionales para sobreescribir conexion local:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

## Ejecucion con Docker

Desde este repositorio:

```bash
docker build -t optiplant-backend .
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=local \
  -e DB_URL='jdbc:postgresql://host.docker.internal:5432/optiplant' \
  -e DB_USERNAME='postgres' \
  -e DB_PASSWORD='postgres' \
  optiplant-backend
```

Nota: para levantar frontend + backend + DB juntos, usar el compose de `optiplant-deploy`.

## API docs
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Endpoints de autenticacion
- `POST /auth/register`
- `POST /auth/login`
