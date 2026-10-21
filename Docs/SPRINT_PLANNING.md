# Sprint Planning · E3 Equivalencias USAL

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-10-14 | jumaluc | Versión inicial: 4 hitos y 8 sprints, basada en requisitos v12.0 y en el contrato de `API.md` v1.0 |

> El stack, los puertos y las convenciones de git están en el [README de la raíz](../README.md).

## Resumen

E3 llega a producción en **4 hitos y 8 sprints de 2 semanas** (19 de octubre de 2026 al 12 de febrero de 2027), y cada hito cierra con un tramo del trámite que funciona de punta a punta.

- **Punto de partida:** el backend es un esqueleto vacío (solo `.gitkeep`), el frontend tiene la base técnica (tokens, cliente HTTP, router, layout público) y el contrato de la API está propuesto en [`API.md`](API.md) pero no acordado.
- **Criterio de avance:** el trámite se construye siguiendo la máquina de estados de RF-ESTADO-001. Cada hito suma un bloque de transiciones (1–5, 6–15, 16–26) y las pantallas que las disparan.
- **Desarrollo correlativo:** en cada sprint el backend implementa los endpoints y el frontend construye las pantallas que los consumen. Para no esperar al backend, el frontend trabaja contra mocks del contrato y los cambia por la API real antes de cerrar el sprint.

## Supuestos y forma de trabajo

| Tema | Supuesto |
|------|----------|
| Duración del sprint | 2 semanas, de lunes a viernes. Pausa de fiestas del 24 de diciembre al 3 de enero |
| Equipo | Al menos una persona en backend y una en frontend. Si el equipo es más chico, cada sprint se estira |
| Ceremonias | Planning al inicio, daily breve, review con demo y retro al cierre de cada sprint |
| Ramas | Gitflow del README: `feature/*` → `develop` → `main`. Un commit toca solo `backend/` o solo `frontend/` |
| Release | Al cierre de cada hito se mergea `develop` a `main` con un tag (`v0.1`, `v0.2`, `v0.3`, `v1.0`) |
| Entorno | Java corre siempre dentro de Docker (no hay JDK ni Maven en la máquina de desarrollo) |

**Definition of Done de cada historia**

- [ ] Endpoint implementado según [`API.md`](API.md), con test de servicio (JUnit 5 + Mockito) y test de integración de la transición.
- [ ] Pantalla conectada a la API real, con estados de carga, vacío, error y éxito (§0.5 de [`frontend-interfaces.md`](frontend-interfaces.md)).
- [ ] Acciones críticas con doble confirmación (RF-AUTH-006).
- [ ] Cambio de estado registrado en `estados_solicitud` y en auditoría.
- [ ] Documentación actualizada en el mismo PR si cambió el contrato, con su entrada en [`GESTION_CAMBIOS.md`](GESTION_CAMBIOS.md).

## Vista general

Cada hito cierra un tramo del trámite de punta a punta. Backend y frontend avanzan en paralelo sobre el mismo bloque de transiciones, y cada hito termina con un release que lleva el trámite un tramo más lejos.

| | H1 · Base y acceso | H2 · Solicitud | H3 · Reunión | H4 · Pago y salida |
|---|---|---|---|---|
| **Sprints** | 1 y 2 | 3 y 4 | 5 y 6 | 7 y 8 |
| **Fechas** | 19 oct a 13 nov | 16 nov a 11 dic | 14 dic a 15 ene | 18 ene a 12 feb |
| **Backend** | Docker, Flyway, login, JWT y RBAC | Motor de estados, bandeja, tomar proceso y mails | Análisis, rondas, reunión, chat e inscripción | Legalización, pago, admin, auditoría y producción |
| **Frontend** | Mocks, componentes, login y perfil | Panel aspirante, carga y revisión de documentos | Elegir horario, chat y decisión de inscripción | Legalizados, cupón, panel admin y auditoría |
| **Release** | `v0.1` · Login por rol | `v0.2` · Hasta `ANAL-PEND` | `v0.3` · Inscripción decidida | `v1.0` · Hasta `E3-INI` |

## Hito 1 · Base técnica y acceso (sprints 1–2)

Al cierre del hito, cada rol puede ingresar al sistema y ver su panel vacío, sobre un backend que corre en Docker con la base de datos migrada.

| Sprint | Backend | Frontend |
|--------|---------|----------|
| **S1** · 19 oct – 30 oct | `pom.xml`, Dockerfile y Docker Compose (backend, PostgreSQL, pgAdmin). Migraciones Flyway de `usuarios`, `roles`, `aspirantes`, `carreras`, `estados` (20) y `auditoria_sistema`. Formato de error común y Swagger UI | Cerrar los puntos abiertos del contrato (§10.3 de `API.md`). Capa de mocks del contrato. Componentes base (inputs, modal de doble confirmación, tabla, badge de estado). Actualizar `frontend-interfaces.md` a los 20 estados de la v12 |
| **S2** · 2 nov – 13 nov | RF-AUTH-001 a 005: registro de aspirante con código por mail, login con bloqueo por intentos, OAuth Google @usal.edu.ar, login de Admin, JWT y RBAC con `revoked_at`. RF-AUTH-007: perfil | Pantallas de registro, verificación de código, login de aspirante, login institucional y perfil. `AuthContext`, rutas protegidas por rol y layout privado con menú por rol |

**Criterios de cierre del Hito 1**

- [ ] Un solo comando del Makefile levanta backend, base y pgAdmin desde cero.
- [ ] El contrato de la API queda acordado y versionado en `API.md`.
- [ ] Los seis roles entran y ven su panel; un usuario revocado no accede.
- [ ] Seed demo con un usuario por rol y las carreras de prueba.

## Hito 2 · Solicitud y revisión documental (sprints 3–4)

Al cierre del hito, un aspirante crea su solicitud y carga documentos, y un Académico la toma, la revisa documento por documento y la acepta o la devuelve (transiciones 1 a 5).

| Sprint | Backend | Frontend |
|--------|---------|----------|
| **S3** · 16 nov – 27 nov | Motor de transiciones: valida rol, permiso de participante y `estadoEsperado`, registra en `estados_solicitud` y auditoría. Tablas `solicitudes` y `documentos_solicitud`. RF-ASP-001 y 002: crear solicitud y subir documentos. Catálogos de carreras y estados | Panel del aspirante. Formulario de nueva solicitud. Carga de documentos con validación de tipo y tamaño. Vista de estado y responsables (RF-ASP-003) con historial del proceso |
| **S4** · 30 nov – 11 dic | Permisos por carrera (`user_career_permissions`). RF-PROC-001 a 004: bandeja ordenada, detalle, tomar proceso y darse de baja. RF-ACAD-001 y 009: marcas tilde/cruz, aceptar o rechazar. RF-ASP-004: recarga selectiva. Servicio de notificaciones por mail en cada cambio de estado (RF-ESTADO-002) | Bandeja del Académico y de Secretaría. Detalle de solicitud. Botón Tomar proceso con manejo de `PROCESO_YA_TOMADO`. Revisión por documento con tilde/cruz. Recarga de documentos marcados del lado del aspirante |

**Criterios de cierre del Hito 2**

- [ ] Una solicitud recorre `SOL-INI` → `DOC-REV` → `DOC-RECH` → `DOC-REV` → `ANAL-PEND` en la demo.
- [ ] Una transición inválida responde 409 `TRANSICION_INVALIDA` y la pantalla lo muestra.
- [ ] Cada cambio de estado deja una fila de auditoría y envía su mail.
- [ ] Dos académicos que toman el mismo proceso a la vez: solo uno queda asignado.

## Hito 3 · Análisis, reunión e inscripción (sprints 5–6)

Al cierre del hito, el Académico propone el análisis preliminar, aspirante y Académico acuerdan la reunión (con rondas de fechas o por chat) y el Académico acepta o rechaza la inscripción (transiciones 6 a 15).

| Sprint | Backend | Frontend |
|--------|---------|----------|
| **S5** · 14 dic – 23 dic | `analisis_academico`, `rondas_reunion`, `horarios_propuestos` y `reuniones`. RF-ACAD-002: matriz, horario de cursada y fechas (multipart). RF-ASP-005: elegir horario o pedir otras fechas. RF-ACAD-003: segunda ronda, rechazo de ronda y fijar fecha, con enlace de Meet | Formulario de análisis preliminar con carga de archivos y fechas. Pantalla del aspirante para elegir horario, pedir otras fechas o rechazar la 2.ª ronda. Vista de reunión pendiente con el enlace de Meet |
| **S6** · 4 ene – 15 ene | RF-MSG-001 a 004: chat grupal por solicitud con historial. RF-ACAD-004: reunión realizada o no asistió, y actualización del análisis en `ANAL-POST`. RF-ACAD-006: aceptar o rechazar inscripción | Chat embebido en el detalle de la solicitud, abierto por defecto en `REU-CHAT`. Modal de Fijar fecha. Acciones de reunión finalizada y no asistió. Decisión de inscripción con doble confirmación. Informe de materias del aspirante (RF-ASP-006) |

S5 es más corto por las fiestas: si no alcanza, la segunda ronda y Fijar fecha pasan a S6.

**Criterios de cierre del Hito 3**

- [ ] La demo recorre los tres caminos de la reunión: fecha en 1.ª ronda, fecha en 2.ª ronda y fecha por chat.
- [ ] No existe forma de llegar a una 3.ª ronda de fechas.
- [ ] Una no asistencia vuelve la solicitud a `REU-FEC-ACAD` y se reprograma.
- [ ] Una inscripción rechazada termina en `INSC-RECH` y no admite más acciones.

## Hito 4 · Legalización, pago, administración y salida (sprints 7–8)

Al cierre del hito, el trámite completo llega a `E3-INI`, el Admin gestiona usuarios, carreras y permisos, el Revisor audita, y el sistema queda listo para producción (`v1.0`, transiciones 16 a 26).

| Sprint | Backend | Frontend |
|--------|---------|----------|
| **S7** · 18 ene – 29 ene | RF-ASP-009 y RF-ACAD-005: indicar legalidad, aviso de legalizados y su revisión. RF-SEC-001 y 002: cupón en PDF con monto, comprobante y verificación del pago. `cupones_pago` y `comprobantes_pago` | Pregunta de legalidad y recarga de legalizados. Revisión de legalizados (reutiliza tilde/cruz del Hito 2). Panel de Secretaría: enviar cupón y controlar el pago. Carga y reenvío de comprobante del aspirante |
| **S8** · 1 feb – 12 feb | RF-ADMIN-001 a 005 y 007: cuentas, carreras, permisos, revocación con reasignación y dashboard de alertas. RF-AUDIT-001 a 003 y RF-REVISOR-001. RF-PROC-005: exportar expedientes en ZIP. Notificaciones en campana. Configuración de producción | Panel de Admin (usuarios, carreras, permisos, alertas). Vista de auditoría con filtros para Admin y Revisor. Campana de notificaciones. Exportación de expedientes. Pruebas de punta a punta con los seis roles |

**Criterios de cierre del Hito 4**

- [ ] Las 26 transiciones tienen test de integración y la demo lleva una solicitud de `SOL-INI` a `E3-INI`.
- [ ] Revocar un permiso corta el acceso al instante y obliga a reasignar sus solicitudes.
- [ ] El Revisor ve la auditoría completa y no puede modificar nada.
- [ ] Despliegue con variables de entorno de producción y backup de la base.

## Riesgos y próximos pasos

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| El contrato de la API no se acuerda en S1 | El frontend trabaja contra mocks que después no coinciden | Reunión de cierre del §10.3 en la primera semana; cambios posteriores pasan por `GESTION_CAMBIOS.md` |
| Se aprueban los cambios CC-001 a CC-005 (permisos por facultad) | Cambian el modelo de permisos y los endpoints de Admin | Decidirlos antes de S4, cuando se implementan los permisos por carrera |
| Credenciales de Google OAuth y del servidor de mail de la USAL | Bloquea el login institucional y las notificaciones | Pedirlas en S1; mientras tanto, mail local de prueba y login simulado en desarrollo |
| Estados posteriores a `E3-INI` sin definir | La descarga del expediente (RF-ASP-008) y la reapertura (RF-ADMIN-006) quedan fuera | Se planifican en un Hito 5 cuando la v13 de requisitos los defina |
| Equipo chico o fiestas | Los sprints S5 y S6 se estiran | Priorizar el camino feliz de cada hito y dejar los caminos alternativos para el sprint siguiente |

**Próximos pasos**

- [ ] Validar este plan con el equipo y ajustar fechas según cuántas personas hay.
- [ ] Cargar las historias del Hito 1 en el tablero (GitHub Projects, Jira o Trello).
- [ ] Agendar la reunión de cierre del contrato de la API para la semana del 19 de octubre.
