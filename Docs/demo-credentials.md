# Credenciales demo

> Solo para desarrollo. Se crean con `SEED_DEMO=true make backend-up-dev` y nunca en producción ([README](../README.md#datos-demo)).

Todas las cuentas usan la contraseña **`1234`**. Es provisoria y fácil a propósito: hay que cambiarla antes de publicar el sistema.

| Rol | Usuario | Contraseña | Pantalla | Endpoint | Notas |
|-----|---------|------------|----------|----------|-------|
| Aspirante | `aspirante@example.com` | `1234` | `/` (inicio) | `POST /auth/login` | |
| Admin General | `admin@usal.edu.ar` | `1234` | `/login-admin` | `POST /auth/admin/login` | |
| Académico | `academico@usal.edu.ar` | `1234` | `/login-personal` | `POST /auth/personal/login` | Con permiso sobre `ING-INF-001` |
| Secretaría | `secretaria@usal.edu.ar` | `1234` | `/login-personal` | `POST /auth/personal/login` | Con permiso sobre `ING-INF-001` |
| Revisor Logs | `revisor@usal.edu.ar` | `1234` | `/login-personal` | `POST /auth/personal/login` | |
| Observador | `observador@usal.edu.ar` | `1234` | `/login-personal` | `POST /auth/personal/login` | Es un Académico sin permisos de carrera |

El personal entra con usuario y contraseña hasta que el sistema se publique con el ingreso con Google (CC-016 de [Gestión de Cambios](GESTION_CAMBIOS.md)).

Tres contraseñas incorrectas seguidas bloquean la cuenta 6 horas (RF-AUTH-002).

El seed no pisa cuentas que ya existen. Si la base local se creó antes del 2026-11-20, las cuentas del personal no tienen contraseña y las del aspirante y el Admin conservan la anterior: para que tomen `1234` hay que resetear la base ([README, Datos Demo](../README.md#datos-demo)).

Con los mocks del frontend (`VITE_USE_MOCKS=true`) valen las mismas cuentas y la misma contraseña; como aspirante se entra con cualquier email.
