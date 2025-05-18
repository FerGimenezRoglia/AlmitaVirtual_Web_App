
# Almita Virtual – Backend API

![](https://github.com/FerGimenezRoglia/AlmitaVirtual_Web_App/blob/main/readme-assets/frontend-preview-1.jpg)

Este repositorio contiene el backend del proyecto **Almita Virtual**, una aplicación web donde cada usuario puede crear entornos personalizados para presentar o compartir sus documentos importantes (como su CV o carta de presentación).

Cada entorno está representado por una "Almita" (personaje animado) que cambia de estado simbólico según la interacción del usuario o visitante. Esta propuesta combina una experiencia técnica robusta con una interfaz estética, sensible y personalizada.

## Descripción funcional

- Cada usuario puede crear múltiples entornos personalizados.
- Cada entorno tiene:
  - Título o profesión.
  - Descripción breve.
  - Color personalizable.
  - Un archivo asociado (PDF, JPG o PNG).
  - Un estado simbólico (IDLE, ACTIVE, REFLECTIVE, EXCITED, INSPIRED).
- El backend gestiona:
  - Registro e inicio de sesión (JWT).
  - Roles (`ROLE_USER`, `ROLE_ADMIN`).
  - CRUD completo de entornos.
  - Subida, descarga y eliminación de archivos.
  - Estados dinámicos según acciones.

## Estados posibles de la Almita

- `IDLE`: Reposo (estado inicial o sin actividad).
- `ACTIVE`: Se ha subido un archivo.
- `REFLECTIVE`: Se ha eliminado el archivo.
- `EXCITED`: Un visitante descargó el archivo.
- `INSPIRED`: Un visitante pulsó “me interesa”.

## Tecnologías utilizadas

- Java 17
- Spring Boot 3.2.4
- Spring Security + JWT
- Spring Data JPA + MySQL
- Swagger (springdoc-openapi)
- JUnit 5 + MockMvc
- Maven

## Estructura de directorios

```
src/
├── main/java/s05/t02/
│   ├── controller/              # Controladores REST
│   ├── exception/               # Manejo de errores global
│   ├── model/                   # Entidades y enums
│   ├── repository/              # Interfaces de persistencia JPA
│   ├── security/                # Configuración de seguridad JWT
│   ├── service/                 # Interfaces y lógica de negocio
│   ├── service/impl/            # Implementaciones de servicios
│   └── AlmitaVirtualApplication.java
├── test/java/s05/t02/           # Tests de integración
│   └── controller/              # Tests por controlador
```

## Configuración de la base de datos

El backend usa MySQL. Crear previamente una base de datos llamada `almita_virtual_db`.

## Cómo ejecutar el backend

```bash
mvn spring-boot:run
```
## Cómo correr los tests

```bash
mvn test
```

## Endpoints de la API

### Autenticación

| Método | Endpoint            | Descripción                    |
|--------|---------------------|--------------------------------|
| POST   | `/auth/register`    | Registro de nuevo usuario      |
| POST   | `/auth/login`       | Inicio de sesión (devuelve JWT)|

### Entornos (privado con JWT)

| Método | Endpoint                  | Descripción                                 |
|--------|---------------------------|---------------------------------------------|
| GET    | `/environments`           | Obtener todos los entornos del usuario      |
| GET    | `/environments/{id}`      | Obtener un entorno por su ID                |
| POST   | `/environments`           | Crear nuevo entorno                         |
| PUT    | `/environments/{id}`      | Actualizar título, descripción o color      |
| DELETE | `/environments/{id}`      | Eliminar entorno (y su archivo si existe)   |

### Archivos (privado con JWT)

| Método | Endpoint                          | Descripción                         |
|--------|-----------------------------------|-------------------------------------|
| POST   | `/environments/{id}/file`         | Subir o reemplazar archivo          |
| DELETE | `/environments/{id}/file`         | Borrar archivo del entorno          |

### Acceso público a entornos

| Método | Endpoint                                        | Descripción                                  |
|--------|-------------------------------------------------|----------------------------------------------|
| GET    | `/public/environments/{id}`                     | Ver entorno sin login                        |
| GET    | `/public/environments/{id}/file`                | Ver o descargar archivo                      |
| PATCH  | `/public/environments/{id}/status`              | Marcar como "me interesa"                    |

## Documentación de API

Una vez ejecutado el proyecto, accede a Swagger UI desde:

```
http://localhost:8080/swagger-ui.html
```

## Notas adicionales

- El backend maneja validaciones, errores personalizados y filtros de autorización.
- Los archivos se alojan en un servicio de almacenamiento en la nube Cloudinary (puede ser reemplazado por Amazon S3 u otro).
- El backend es 100% stateless, usando JWT para mantener sesiones seguras.

---

### 🛠️ Utilidad técnica

Este proyecto incluye un script ejecutable que imprime una descripción detallada del backend, incluyendo el flujo del sistema, los estados de la Almita y los endpoints principales.

Para ejecutarlo, abrí una terminal desde la raíz del proyecto y corré:

```bash
./describe-app.sh
