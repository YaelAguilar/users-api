## Users API - Prueba Técnica Chakray

REST API desarrollada como solución a la **“Prueba Técnica Desarrollador 1 (98).pdf”** de Chakray (`Java Developer Jr-Mid`), usando **Java 21**, **Spring Boot** y **Maven**.  
La API gestiona usuarios en memoria (lista con 3 usuarios iniciales) y expone operaciones de listado, filtrado, CRUD parcial y autenticación por `tax_id`.

---

### Repositorio GitHub

El código fuente también está versionado en GitHub (reemplazar con la URL real del repositorio):

- `https://github.com/YaelAguilar/users-api`

---

### Requisitos

- **Java**: JDK 17 o 21 (se usó Java 21).
- **Maven**: no es necesario tenerlo instalado; el proyecto incluye **Maven Wrapper** (`mvnw` / `mvnw.cmd`).
- **Docker** (opcional): para construir y ejecutar la imagen Docker.

---

### Cómo ejecutar la aplicación

#### Opción 1: Maven (sin Docker)

Desde la carpeta raíz del proyecto (`users-api`):

- **Windows (PowerShell / CMD)**:

```bash
cd users-api
.\mvnw.cmd spring-boot:run
```

- **Linux / macOS (bash/zsh)**:

```bash
cd users-api
./mvnw spring-boot:run
```

La API quedará disponible en:

- `http://localhost:8080`

#### Opción 2: Docker

Desde la carpeta raíz del proyecto (`users-api`):

1. **Construir la imagen**

```bash
docker build -t users-api .
```

2. **Ejecutar el contenedor**

```bash
docker run --rm -p 8080:8080 users-api
```

La API también quedará expuesta en `http://localhost:8080`.

---

### Cómo ejecutar los tests

Desde la raíz del proyecto:

- **Windows**:

```bash
cd users-api
.\mvnw.cmd test
```

- **Linux / macOS**:

```bash
cd users-api
./mvnw test
```

Se ejecutan los tests JUnit de `UserService`, que validan:

- Inicialización de los 3 usuarios.
- Ordenamiento por distintos campos.
- Filtros (`filter` con `co`, `eq`, `ew`).
- Creación, actualización, borrado de usuarios.
- Búsqueda por `tax_id`.
- Validaciones de `tax_id` y `phone` en `PATCH`.

---

### Documentación Swagger / OpenAPI

Con la aplicación levantada (Maven o Docker), puedes acceder a:

- **Swagger UI**:  
  `http://localhost:8080/swagger-ui.html`

Ahí se visualizan y pueden probarse todos los endpoints de la API con sus modelos de request/response.

---

### Colección de Postman

En la carpeta raíz del proyecto se incluye la colección:

- `chakray.postman_collection.json`

Para usarla:

1. Abrir Postman.
2. Importar el archivo `chakray.postman_collection.json`.
3. Ejecutar los requests que ya vienen configurados para:
   - `GET /users` (sin y con `sortedBy`).
   - `GET /users?filter=...`.
   - `POST /users`.
   - `PATCH /users/{id}`.
   - `DELETE /users/{id}`.
   - `POST /login`.

> Nota: todos los requests apuntan a `http://localhost:8080`, por lo que puedes usarlos tanto con la app levantada por Maven como dentro de Docker.

---

### Endpoints principales

#### 1. Listar usuarios con ordenamiento

- **GET** `http://localhost:8080/users?sortedBy={campo}`  
  - **Parámetro opcional** `sortedBy`: `email | id | name | phone | tax_id | created_at`  
  - Si `sortedBy` no se envía o es vacío, simplemente devuelve la lista sin ordenar explícitamente.

Ejemplo:

```text
GET /users?sortedBy=name
```

#### 2. Listar usuarios con filtro

- **GET** `http://localhost:8080/users?filter={campo}+{operador}+{valor}`  
  - `campo`: `email | id | name | phone | tax_id | created_at`  
  - `operador`:  
    - `co` = contains  
    - `eq` = equals  
    - `sw` = starts with  
    - `ew` = ends with  

Ejemplos (del enunciado de la prueba):

- `GET /users?filter=name+co+user`  
- `GET /users?filter=email+ew+mail.com`  
- `GET /users?filter=phone+sw+555`  
- `GET /users?filter=tax_id+eq+AARR990101XXX`

#### 3. Crear usuario

- **POST** `http://localhost:8080/users`
- **Body (JSON)**:

```json
{
  "email": "user4@mail.com",
  "name": "user4",
  "phone": "+1 5555555558",
  "password": "password123",
  "tax_id": "DDRR990101WWW",
  "addresses": []
}
```

Comportamiento relevante:

- `id` se genera como `UUID` aleatorio.
- `created_at` se establece con la **hora actual en la zona horaria de Madagascar** (`Indian/Antananarivo`) en formato `dd-MM-yyyy HH:mm`.
- `password` se guarda cifrada con **AES-256** y **no se devuelve en la respuesta**.
- Se valida que:
  - `tax_id` tenga formato RFC.
  - `phone` tenga 10 dígitos, pudiendo incluir código de país.
  - `tax_id` sea **único** (no duplicado).

#### 4. Actualizar parcialmente un usuario

- **PATCH** `http://localhost:8080/users/{id}`
- **Body (JSON)** con los campos a actualizar (parcial):

```json
{
  "name": "user4updated"
}
```

Campos actualizables:

- `email`, `name`, `phone`, `tax_id`, `password`.

Reglas adicionales:

- Si se actualiza `phone`, se vuelve a validar el formato.
- Si se actualiza `tax_id`, se valida:
  - Formato RFC.
  - Unicidad (no puede existir en otro usuario).
- Si se actualiza `password`, se vuelve a cifrar con AES-256.
- La respuesta nunca incluye el campo `password`.

#### 5. Eliminar usuario

- **DELETE** `http://localhost:8080/users/{id}`

Respuestas:

- `204 No Content` si se elimina correctamente.
- `404 Not Found` si el usuario no existe.

#### 6. Login

- **POST** `http://localhost:8080/login`
- **Body (JSON)**:

```json
{
  "tax_id": "AARR990101XXX",
  "password": "password123"
}
```

Comportamiento:

- Se busca el usuario por `tax_id`.
- Se desencripta la contraseña almacenada (AES-256) y se compara con la contraseña recibida.
- Si las credenciales son válidas, se responde con:

```json
{
  "message": "Login successful",
  "tax_id": "AARR990101XXX",
  "name": "user1"
}
```

- Si las credenciales son incorrectas, se responde con `401 Unauthorized` y un mensaje de error.

---

### Detalles técnicos relevantes para la prueba

- **Tecnología**:
  - Java 21, Spring Boot 3, Maven.
- **Almacenamiento en memoria**:
  - Lista con 3 usuarios iniciales, con estructura idéntica a la del ejemplo del PDF de la prueba.
- **Seguridad de contraseñas**:
  - Cifrado con **AES-256** (clave de 32 bytes).
  - Contraseñas nunca se incluyen en respuestas JSON.
- **Validaciones**:
  - `tax_id` con formato RFC (regex).
  - `phone` con 10 dígitos, permitiendo código de país (“AndresFormat” aproximado vía regex).
  - `tax_id` único tanto en creación (`POST`) como en actualización (`PATCH`).

Con este proyecto y este `README.md` se cubren los requisitos funcionales y extras solicitados en la **Prueba Técnica Desarrollador 1 (98).pdf** de Chakray.

