# CHN — Sistema de Gestión de Préstamos Bancarios

API REST para gestión de clientes, solicitudes de préstamo, aprobación/rechazo, préstamos
aprobados y pagos. Spring Boot 3 (Java 21) + SQL Server (Transact-SQL) + Flyway, con seguridad
JWT por matriz de permisos. Diseño documentado en [`docs/`](./docs).

## Stack

- **Java 21**, **Spring Boot 3.3** (Web, Data JPA, Validation, Security, Actuator)
- **SQL Server 2022** (T-SQL), esquema versionado con **Flyway**
- **JWT** (jjwt) + autorización por permisos `modulo.accion`
- **OpenAPI / Swagger UI**
- **Docker / Docker Compose** (build multi-stage; no requiere Java/Maven local)

## Estructura del repositorio

```
chn/
├── backend/                     # API Spring Boot (pom.xml, src/, Dockerfile)
├── frontend/                    # SPA Angular 18 + Tailwind (Dockerfile + nginx)
├── docs/                        # Casos de uso, procesos y MER (PDF) + README de diseño
├── .github/workflows/           # CI: build y publicación de imágenes en GHCR
├── docker-compose.yml           # Stack local: SQL Server + API + frontend (compila imágenes)
└── docker-compose.registry.yml  # Stack desde imágenes ya publicadas en GHCR
```

## Ejecutar

Las credenciales no se versionan: se leen de un archivo `.env` (ignorado por git). Crea el tuyo a
partir de la plantilla y levanta el stack:

```bash
cp .env.example .env          # Linux/macOS  (Windows: Copy-Item .env.example .env)
docker compose up --build
```

Esto levanta SQL Server y la aplicación. La app crea la base de datos `chn_prestamos`
(reintentando hasta que SQL Server esté listo), ejecuta las migraciones Flyway y siembra
catálogos, roles y permisos. Los usuarios por defecto se crean al arrancar.

Levanta **los tres servicios** (SQL Server, backend y frontend) con un solo comando.

- **Frontend (Angular):** `http://localhost:4200`
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`

### Variables de entorno (definidas en `.env`)

Ver [`.env.example`](./.env.example) para la lista completa. Las **requeridas** (sin valor por
defecto, no se versionan):

| Variable | Uso |
|----------|-----|
| `DB_PASSWORD` | Contraseña de `sa` y de la app |
| `JWT_SECRET` | Firma de tokens (mínimo 256 bits / 32+ caracteres) |
| `ADMIN_PASSWORD` | Contraseña del usuario `admin` |
| `OPERADOR_PASSWORD` | Contraseña del usuario `operador` |

> Opcionales (`DB_URL`, `DB_USERNAME`, `JWT_EXP_MINUTES`, `ADMIN_USERNAME`, `OPERADOR_USERNAME`)
> tienen valor por defecto en `application.yml`.

## Despliegue Local

Requisito común: **Docker Desktop** en ejecución. Hay dos formas de levantarlo.

### Opción A — Compilado local (clonar y compilar)

Para ejecutar el proyecto desde el código. No requiere autenticación ni imágenes publicadas;
compila todo en local.

```bash
git clone https://github.com/DiiAns23/prueba-chn.git
cd prueba-chn
cp .env.example .env            # Windows PowerShell: Copy-Item .env.example .env
docker compose up --build -d
```

Compila backend (Maven) y frontend (Angular) y levanta los tres servicios:
- Frontend: `http://localhost:4200` · API: `http://localhost:8080` · Swagger: `/swagger-ui.html`

Para detener: `docker compose down` (agrega `-v` para borrar también los datos de la BD).

### Opción B — Solo para probar (imágenes ya publicadas en GHCR, sin compilar)

La forma más rápida de levantarlo sin compilar. El CI publica `ghcr.io/diians23/chn-backend` y
`ghcr.io/diians23/chn-frontend`. Necesitas solo el repo (por los `docker-compose` y el `.env`),
no el código:

```bash
git clone https://github.com/DiiAns23/prueba-chn.git
cd prueba-chn
cp .env.example .env
export GHCR_OWNER=diians23                 # Windows PowerShell: $env:GHCR_OWNER = "diians23"
docker compose -f docker-compose.registry.yml up -d
```

> **¿Hace falta login?** Las imágenes de GHCR son **privadas** por defecto: para bajarlas habría que
> `docker login ghcr.io` con un token (`read:packages`). Para que cualquiera las descargue **sin
> autenticarse**, márcalas como **públicas** en GitHub (Packages → cada paquete → *Package settings*
> → *Change visibility* → *Public*). Si prefieres no exponerlas, usa la **Opción A** (clonar y
> compilar), que no necesita acceso al registry.

### Levantar servicio por servicio

`docker compose` resuelve las dependencias (`depends_on`) automáticamente:

```bash
docker compose up -d sqlserver    # solo la base de datos
docker compose up -d app          # solo la API (arranca también su BD; sin frontend)
docker compose up -d frontend     # frontend + API + BD (toda la cadena)
```

Para levantar **solo la API** usa `docker compose up -d app`. Añade `--build` si compilas en local
(Opción A), o `-f docker-compose.registry.yml` para usar las imágenes publicadas.

## Usuarios por defecto

| Usuario | Contraseña | Rol | Permisos |
|---------|-----------|-----|----------|
| `admin` | la de `ADMIN_PASSWORD` (tu `.env`) | ADMINISTRADOR | seguridad.* |
| `operador` | la de `OPERADOR_PASSWORD` (tu `.env`) | OPERADOR | clientes.*, prestamos.*, pagos.* |
| `cliente` | la de `CLIENTE_PASSWORD` (tu `.env`) | CLIENTE | autoservicio: sus solicitudes/préstamos y su contacto |

> El usuario `cliente` (autoservicio) solo se siembra si defines `CLIENTE_PASSWORD`: se crea un
> cliente demo y se enlaza. También pueden crearse más usuarios CLIENTE vía el módulo de seguridad
> (administrado por `admin`).

## Autenticación

```bash
# 1) Login -> token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"nombreUsuario":"operador","contrasena":"<OPERADOR_PASSWORD>"}'

# 2) Usar el token en las llamadas
curl http://localhost:8080/api/v1/clientes -H "Authorization: Bearer <TOKEN>"
```

## Endpoints principales

| Método | Ruta | Permiso |
|--------|------|---------|
| POST | `/api/v1/auth/login` | público |
| POST/GET/PUT/DELETE | `/api/v1/clientes` | `clientes.*` |
| POST/GET | `/api/v1/solicitudes` | `prestamos.crear` / `prestamos.leer` |
| POST | `/api/v1/solicitudes/{id}/aprobar` · `/rechazar` | `prestamos.aprobar` |
| GET | `/api/v1/prestamos?clienteId=` | `prestamos.leer` |
| GET | `/api/v1/prestamos/{id}/saldo` | `pagos.leer` |
| POST | `/api/v1/prestamos/{id}/pagos` | `pagos.crear` |

## Decisiones de diseño

- **Soft delete** en todas las entidades (nunca borrado físico; auditoría bancaria).
- **Sin cálculo de intereses** (no está en el enunciado), pero el modelo es escalable
  (`tasa_interes`, `monto_total`, tabla `cuota_prestamo`).
- **Seguridad por matriz de permisos**: roles agrupadores + excepciones por usuario (`granted`),
  resueltos con `@RequierePermiso`.

El **script de BD** entregable son las migraciones T-SQL en
[`backend/src/main/resources/db/migration`](./backend/src/main/resources/db/migration).

## CI/CD e imágenes (GitHub Actions + GHCR)

El workflow [`.github/workflows/ci.yml`](./.github/workflows/ci.yml) **construye y publica** las
imágenes de `backend` y `frontend` en GitHub Container Registry (GHCR) **solo cuando el código llega
a `main`** (push/merge de un PR) o al lanzarlo manualmente (*workflow_dispatch*). No se ejecuta al
subir ramas ni al abrir *pull requests*. **No despliega a producción**: deja las imágenes listas
para ejecutarse sin compilar (ver [Despliegue Local](#despliegue-local), Opción B).

## Pruebas

```bash
cd backend && mvn test   # requiere Docker para Testcontainers (SQL Server real)
```
