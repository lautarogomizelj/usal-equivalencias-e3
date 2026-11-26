# Guía de pruebas · E3 Equivalencias USAL

Cómo levantar el sistema completo en una máquina y recorrerlo de punta a punta con las cuentas demo. Los puertos, el stack y el detalle de cada comando están en el [README](../README.md#cómo-correr-el-proyecto); acá va solo lo necesario para probar.

## Control de versiones

| Versión | Fecha | Autor | Cambios |
|---------|-------|-------|---------|
| 1.0 | 2026-11-26 | jumaluc | Versión inicial: puesta en marcha, cuentas, recorrido completo de una solicitud, pruebas por rol y cómo empezar de cero. |

## 1. Qué hace falta

- Docker Desktop abierto.
- Node 24 y npm.
- Git Bash (o cualquier terminal con `make`). Sin `make`, usar los comandos de la sección 2.1.
- Los puertos 5173, 8080, 5432, 5050 y 8025 libres.

## 2. Levantar todo

Desde la raíz del repositorio, en la rama `develop`:

```bash
git checkout develop && git pull

# Backend, base de datos y mail de prueba, con las cuentas demo
SEED_DEMO=true make backend-up-dev

# Frontend contra el backend real
cp frontend/.env.example frontend/.env
```

Abrir `frontend/.env` y dejar `VITE_USE_MOCKS=false` (el ejemplo viene en `true`, que usa datos simulados y no toca el backend). Después:

```bash
make install
make dev
```

La primera vez el backend tarda unos minutos en compilar. Cuando está listo:

| Qué | Dónde |
|-----|-------|
| El sistema | http://localhost:5173 |
| Mails que manda el sistema (códigos y avisos) | http://localhost:8025 |
| Documentación de la API | http://localhost:8080/swagger-ui.html |

El sistema se abre siempre como `localhost`, no como `127.0.0.1`: con otro nombre el login falla.

### 2.1 Sin `make`

```bash
cp backend/src/main/resources/application.yml.example backend/src/main/resources/application.yml
SEED_DEMO=true docker compose up -d --build
cd frontend && npm ci && npm run dev
```

En PowerShell, la variable se define antes: `$env:SEED_DEMO = 'true'`.

### 2.2 Apagar y empezar de cero

- Apagar: `Ctrl+C` en la terminal del frontend y `make down` (o `docker compose down`).
- Empezar de cero: después de apagar, borrar la carpeta `data/` de la raíz y volver a levantar. Se pierden las solicitudes, los archivos y las cuentas creadas; las cuentas demo se crean de nuevo.

## 3. Cuentas

Todas usan la contraseña `1234`. La tabla completa está en [`demo-credentials.md`](demo-credentials.md).

| Rol | Usuario | Por dónde entra |
|-----|---------|-----------------|
| Aspirante | `aspirante@example.com` | `/` (pantalla de inicio) |
| Académico | `academico@usal.edu.ar` | `/login-personal` (enlace "¿Sos académico?") |
| Académico 2 | `academico2@usal.edu.ar` | `/login-personal` |
| Secretaría | `secretaria@usal.edu.ar` | `/login-personal` |
| Revisor | `revisor@usal.edu.ar` | `/login-personal` |
| Observador (Académico sin carreras) | `observador@usal.edu.ar` | `/login-personal` |
| Admin | `admin@usal.edu.ar` | `/login-admin` (no hay enlace: se escribe la dirección) |

Tres cosas para tener en cuenta:

- **Tres contraseñas incorrectas seguidas bloquean la cuenta 6 horas.** Para probar el bloqueo conviene usar una cuenta creada para eso. El Admin ve las cuentas bloqueadas en su inicio y las puede desbloquear.
- La base arranca **sin solicitudes**: se crean durante la prueba.
- Para tener dos usuarios a la vez, abrir uno en una ventana normal y el otro en una de incógnito.

## 4. Recorrido completo de una solicitud

Es el camino feliz, de la creación al pago aceptado. Cada paso deja la solicitud en el estado indicado y manda un mail al que le toca actuar (se ve en http://localhost:8025) y un aviso en su campana.

Los archivos pueden ser cualquier PDF o JPG de hasta 10 MB.

| # | Quién | Qué hace | Estado en que queda |
|---|-------|----------|---------------------|
| 1 | Aspirante | "Nueva solicitud": completa los datos, elige Ingeniería en Informática y confirma | Solicitud iniciada |
| 2 | Aspirante | Carga el analítico, el plan de estudios y al menos un programa (con su materia) y toca "Enviar documentación" | Documentación pendiente de revisión |
| 3 | Académico | Abre la solicitud desde "Solicitudes" y toca "Tomar proceso" | (igual) |
| 4 | Académico | Marca cada documento como correcto y toca "Aceptar documentación" | Análisis preliminar pendiente |
| 5 | Académico | Sube la matriz de equivalencias, escribe el posible horario, agrega uno o más horarios de reunión (de 10 a 17 h, desde mañana y hasta 30 días) y toca "Enviar al aspirante" | Reunión pendiente de fechas (1ª ronda) |
| 6 | Aspirante | "Seleccionar fecha de reunión": elige un horario | Reunión pendiente de realización |
| 7 | Académico | Carga el enlace de Meet (`https://meet.google.com/...`) | (igual) |
| 8 | Académico | Indica "Sí, la reunión finalizó" y toca "Continuar proceso" | Análisis pos reunión |
| 9 | Académico | Elige "Aceptar el análisis preliminar y las equivalencias" y guarda la decisión | Inscripción aceptada |
| 10 | Aspirante | Responde "Sí, están legalizados" | Análisis de documentos legalizados |
| 11 | Académico | Marca los documentos como correctos, toca "Aceptar documentación" e informa las materias reconocidas | Solicitud de cupón de pago |
| 12 | Secretaría | "Tomar proceso", escribe el monto y toca "Generar y enviar cupón" | Pago de cupón por aspirante |
| 13 | Aspirante | Descarga el cupón, carga el comprobante y lo envía | Control de pago |
| 14 | Secretaría | Elige "Pago correcto" y guarda la verificación | Inicio de E3 |

En cualquier momento, las pestañas del detalle muestran los documentos, los datos, el chat entre el aspirante y su responsable, y el historial.

## 5. Caminos alternativos

Se prueban sobre otra solicitud, en el paso indicado del recorrido.

| En el paso | Qué probar | Qué tiene que pasar |
|-----------|-----------|---------------------|
| 1 | El aspirante cancela la solicitud antes de enviarla | Queda "Solicitud cancelada" y se borran sus archivos |
| 2 | Subir un archivo que no es PDF ni JPG, o de más de 10 MB | Lo rechaza con un mensaje; "Enviar documentación" sigue deshabilitado si falta algo |
| 3 | El Académico 2 intenta tomar un proceso que ya tomó el Académico | Le avisa que otro usuario lo tomó antes |
| 3 | El Académico toca "Darse de baja" | El proceso vuelve a quedar sin responsable |
| 4 | Marcar un documento como "Debe recargar", o agregar un documento faltante, y "Rechazar documentación" | Documentación rechazada: el aspirante recarga solo lo marcado y la vuelve a enviar |
| 6 | El aspirante dice que no puede en ningún horario | El Académico propone una 2ª ronda; si el aspirante también la rechaza, la fecha se acuerda por chat y el Académico la fija |
| 8 | El Académico indica que el aspirante no se presentó | Se reprograma con una nueva ronda de fechas |
| 9 | El Académico rechaza la inscripción con su motivo | Estado final de rechazo; el aspirante ve el motivo y el chat queda cerrado |
| 10 | El aspirante responde que todavía no legalizó | Queda en espera hasta que avisa que ya los legalizó |
| 11 | El Académico rechaza los documentos legalizados | El aspirante recarga los marcados |
| 14 | Secretaría rechaza el pago con un comentario | El aspirante ve el motivo y carga otro comprobante; el anterior se conserva |

## 6. Qué mirar con cada rol

**Aspirante.** Registro desde "Creá tu cuenta" (el código de 6 dígitos llega a http://localhost:8025), recuperación de contraseña, inicio con contadores, "Mis solicitudes", campana y "Mi perfil" (datos, contraseña, sesiones activas e historial de accesos).

**Académico y Secretaría.** Inicio con su trabajo pendiente, "Solicitudes" con buscador, filtros, vistas y "Exportar" (baja un ZIP con los expedientes que muestra la lista), y "Descargar expediente" en el detalle.

**Observador.** Ve las solicitudes en modo lectura y no puede tomar procesos.

**Revisor.** Ve todo en modo lectura, sin botones de acción, y tiene "Auditoría" en el menú y como pestaña del detalle.

**Admin.**

- Inicio: procesos sin responsable y cuentas bloqueadas.
- Usuarios: crear una cuenta `@usal.edu.ar` con su contraseña inicial, editarla, asignarle o revocarle carreras y desactivarla. Una cuenta desactivada no puede entrar. Revocar la carrera a quien tiene procesos activos pide confirmarlo.
- Carreras: crear, editar y desactivar. Una carrera desactivada deja de aparecer en "Nueva solicitud".
- Detalle de una solicitud: "Asignar responsable", también a alguien sin acceso a la carrera (crea un permiso temporal y pide el motivo). Además puede ejecutar las acciones del Académico y de Secretaría.
- Auditoría: registro de todas las acciones, con filtros por usuario, acción, entidad y fechas.

**Cualquiera.** Entrar a una dirección que no le corresponde (por ejemplo, `/admin/usuarios` como Académico) lo devuelve a su inicio. Sin sesión, cualquier pantalla privada lleva al inicio. La página se tiene que poder usar también en el celular (o achicando la ventana).

## 7. Lo que todavía no está

Para no reportarlo como error:

- **Ayuda** muestra "Próximamente".
- **Ingreso con Google**: el personal entra con usuario y contraseña.
- **Reapertura de expedientes** y **permisos por facultad**: sin hacer.
- **Avatares**: se muestran las iniciales.
- Después de "Inicio de E3" la solicitud no tiene más pasos.

## 8. Si algo falla

| Síntoma | Qué revisar |
|---------|-------------|
| El login dice que el usuario o la contraseña no son correctos con una cuenta demo | Que el backend se haya levantado con `SEED_DEMO=true`. Si la base es de antes, empezar de cero (sección 2.2) |
| Aparecen solicitudes de ejemplo que nadie creó | `frontend/.env` quedó con `VITE_USE_MOCKS=true`. Cambiarlo y reiniciar `make dev` |
| "Tu sesión venció" apenas se entra, o no carga nada | Abrir el sistema en `http://localhost:5173` (ese puerto y ese nombre) y mirar que el backend responda en http://localhost:8080/actuator/health |
| La cuenta quedó bloqueada | Desbloquearla desde el inicio del Admin, o esperar 6 horas |
| No llega un mail | Todos van a http://localhost:8025, no a casillas reales |
| El backend no arranca | `make backend-logs` |

Al reportar un error sirve indicar: con qué cuenta, en qué solicitud (su código `EXP-…`), en qué estado estaba y qué mensaje apareció.
