# reto-optiplant-backend

# Optiplant Backend Challenge

API Backend desarrollada con **Spring Boot** para autenticación y gestión de usuarios.

## Tecnologías utilizadas

* Java 21
* Spring Boot
* Spring Security
* JWT (JSON Web Token)
* PostgreSQL (Neon Cloud Database)
* Swagger / OpenAPI
* Gradle

## Funcionalidades

* Registro de usuarios
* Inicio de sesión con autenticación JWT
* Encriptación de contraseñas con BCrypt
* Documentación de la API con Swagger
* Persistencia de datos en PostgreSQL en la nube

## Estructura del proyecto

El proyecto sigue una arquitectura por capas para separar responsabilidades:

controller
service
repository
entity
dto
security
config

### Descripción de cada capa

**controller**
Contiene los endpoints de la API REST y gestiona las solicitudes HTTP.

**service**
Contiene la lógica de negocio de la aplicación.

**repository**
Encargado del acceso a la base de datos mediante Spring Data JPA.

**entity**
Define las entidades que representan las tablas de la base de datos.

**dto**
Objetos utilizados para transferir datos entre cliente y servidor.

**security**
Configuraciones y servicios relacionados con autenticación y generación de tokens JWT.

**config**
Configuraciones generales del proyecto como beans de seguridad.

## Endpoints principales

### Registro de usuario

POST /auth/register

Permite registrar un nuevo usuario en el sistema.

### Inicio de sesión

POST /auth/login

Valida las credenciales del usuario y devuelve un **token JWT** que será utilizado para acceder a endpoints protegidos.

## Seguridad

La autenticación se maneja mediante **JSON Web Tokens (JWT)**.

Los endpoints protegidos requieren enviar el token en el header:

Authorization: Bearer <token>

## Base de datos

La aplicación utiliza **PostgreSQL** alojado en **Neon**, permitiendo una base de datos accesible desde la nube.

## Documentación de la API

La documentación de la API está disponible mediante **Swagger** en:

http://localhost:8080/swagger-ui/index.html

## Ejecución del proyecto

1. Clonar el repositorio

2. Configurar las variables de conexión a la base de datos en `application.properties` o `application.yml`.

3. Ejecutar el proyecto con:

./gradlew bootRun

## Posibles mejoras

* Autorización basada en roles (RBAC)
* Implementación de Refresh Tokens
* Contenerización con Docker
* Pruebas unitarias y de integración
* Manejo global de excepciones
