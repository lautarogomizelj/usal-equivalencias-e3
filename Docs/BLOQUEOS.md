# Bloqueos y pendientes externos

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-11-17 | jumaluc | Versión inicial: cuenta de mail, credenciales de Google y galería de avatares |

Cosas que el equipo necesita conseguir o que alguien de afuera tiene que definir para poder seguir. No son tareas de código: el código ya está, o no se puede terminar hasta tener esto. Cada punto dice qué traba, qué hace falta y cómo se trabaja mientras tanto.

Las dudas de criterio para charlar con el profesor van en [Temas para el profesor](TEMAS_PROFESOR.md). El estado del proyecto está en el [README](../README.md#estado-actual).

## Pendientes

| # | Fecha | Qué falta | Qué traba | Qué hace falta conseguir o decidir | Cómo se trabaja mientras tanto | Estado |
|---|-------|-----------|-----------|------------------------------------|--------------------------------|--------|
| 1 | 2026-11-17 | **Cuenta de mail para enviar los correos del sistema** | El sistema manda mails de verdad en el registro (código de 6 dígitos), en la recuperación de contraseña y, desde el Sprint 4, en cada cambio de estado. Sin una cuenta real no se puede desplegar ni probar con correos reales. | Definir **desde qué dirección** sale el correo (hoy está puesto `no-responder@usal.edu.ar`, que no existe) y con **qué servicio** se envía: una casilla de la USAL, una cuenta de Gmail del equipo u otro proveedor. Con eso se completan `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` y `MAIL_FROM` ([README, Configuración de Ambiente](../README.md#configuración-de-ambiente)). | Mailpit: un servidor de mail de prueba que levanta con el backend y muestra los correos en `http://localhost:8025`. Ningún mail sale a internet. | Pendiente |
| 2 | 2026-11-17 | **Credenciales de Google para el login del personal** | Académico, Secretaría y Revisor entran solo con Google (RF-AUTH-003). El recorrido está implementado en backend y frontend, pero Google lo rechaza con "Error 401: invalid_client" porque no hay una aplicación registrada. No se pudo probar el ingreso ni el aviso de "Usuario no registrado". | Registrar la aplicación en Google Cloud (pantalla de consentimiento y cliente OAuth) y cargar `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET`. Hay que definir **quién la registra**: lo ideal es una cuenta de la USAL, para poder limitar el acceso al dominio `usal.edu.ar`; si no se consigue, una cuenta del equipo sirve para la demo. | `POST /auth/dev/login` y el bloque "Solo en desarrollo" del login del personal, con las cuentas de [`demo-credentials.md`](demo-credentials.md). | Pendiente |
| 3 | 2026-11-17 | **Ilustraciones para la galería de avatares** | RF-AUTH-007 pide que cada usuario elija un avatar de una galería por género. El backend ya guarda y valida la clave (`hombre-02`, `mujer-03`), pero no hay imágenes, así que Mi perfil no ofrece la elección. | Las ilustraciones (el equipo de diseño o un set con licencia libre) y cuántas por género. | El avatar son las iniciales del nombre y el apellido. | Pendiente |
