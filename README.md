# CHN Examen (Monorepo)

Este repositorio agrupa:

- `chn-examen-api` (Spring Boot + SQL Server)
- `chn-examen-web` (Angular)

## Levantar todo con Docker Compose

1. Copia variables de entorno:

```bash
cp .env.example .env
```

2. Levanta todo:

```bash
docker compose up --build
```

Servicios:

- Frontend: `http://localhost:4200`
- Backend API: `http://localhost:8080/api`
- SQL Server: `localhost:1433`

## Script SQL automático

`docker-compose.yml` incluye un servicio `db-init` que ejecuta automáticamente `chn-examen-api/script.sql` después de que SQL Server esté disponible.

El script está preparado para ser idempotente (crea BD/tablas solo si no existen), por lo que puedes volver a levantar sin romper por objetos ya creados.

## Reinicio limpio (opcional)

Si quieres reiniciar base de datos desde cero:

```bash
docker compose down -v
docker compose up --build
```
