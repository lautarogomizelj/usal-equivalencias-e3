# Temas para charlar con el profesor

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-11-17 | jumaluc | Versión inicial, con la duda sobre el acceso al login de Admin |

Dudas del equipo que necesitan la opinión del profesor antes de darse por cerradas. Cada tema dice qué se hizo mientras tanto, para que el desarrollo no quede frenado. Cuando un tema se resuelve, se anota la respuesta acá y se actualiza el documento fuente que corresponda (con su fila en [Gestión de Cambios](GESTION_CAMBIOS.md) si cambia un requisito).

El estado del proyecto está en el [README](../README.md#estado-actual).

## Temas

| # | Fecha | Tema | Contexto | Qué se hizo mientras tanto | Estado | Respuesta |
|---|-------|------|----------|----------------------------|--------|-----------|
| 1 | 2026-11-17 | **Acceso al login de Admin:** ¿cómo llega el Admin General a su pantalla de login? | Los requisitos definen tres logins (RF-AUTH-002, 003 y 004) y `frontend-interfaces.md` los trata como pantallas separadas (§3.4, §3.6 y §3.7), pero ningún documento dice en qué ruta está el login de Admin ni desde dónde se llega. Las opciones son: un enlace desde el login del personal, un enlace en la pantalla de inicio, o ningún enlace público (el Admin entra escribiendo la dirección). Mostrar el enlace es más cómodo; ocultarlo expone menos la entrada de la cuenta con más permisos. | El login de Admin está en `/login-admin` y se llega con el enlace "¿Sos administrador? Ingresá acá" del login del personal (`/login-personal`). La pantalla de inicio no cambia. | Pendiente | |
