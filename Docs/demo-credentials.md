# Credenciales demo

> Solo para desarrollo. Se crean con `SEED_DEMO=true make backend-up-dev` y nunca en producción ([README](../README.md#datos-demo)).

| Rol | Cuenta | Cómo entra |
|-----|--------|------------|
| Aspirante | `aspirante@example.com` | `POST /auth/login` con `Demo1234!` |
| Admin General | `admin@usal.edu.ar` | `POST /auth/admin/login` con `Demo1234!` |
| Académico | `academico@usal.edu.ar` | `POST /auth/dev/login`, con permiso sobre `ING-INF-001` |
| Secretaría | `secretaria@usal.edu.ar` | `POST /auth/dev/login`, con permiso sobre `ING-INF-001` |
| Revisor Logs | `revisor@usal.edu.ar` | `POST /auth/dev/login` |
| Observador | `observador@usal.edu.ar` | `POST /auth/dev/login`: es un Académico sin permisos de carrera |

El personal entra con Google en producción. `POST /auth/dev/login` lo reemplaza en desarrollo y recibe `{ "email": "..." }`.
