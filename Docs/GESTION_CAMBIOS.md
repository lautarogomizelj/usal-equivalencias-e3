# Gestión de Cambios

> Los documentos fuente (`Sistema_Equivalencias_Requisitos.md`, `frontend-interfaces.md`, `ARQUITECTURA_E3.md`) tienen su propia tabla de **Control de versiones** arriba de todo. Este documento registra *por qué* cambió algo, especialmente cuando el cambio aparece durante el desarrollo y no estaba previsto en la especificación original.

## Propósito

Durante el desarrollo aparecen cambios de requisito que la especificación no contemplaba. El objetivo de este documento es:

- Registrar cada cambio con su motivo, alcance y estado.
- Evitar crear un archivo nuevo por cada cambio: el documento fuente se **edita in place** y se agrega una fila en su tabla de control de versiones.
- Dejar trazabilidad de qué requisito cambió, cuándo, quién lo pidió y en qué versión quedó reflejado.

## Alcance

| Tipo | Cuándo se usa |
|------|---------------|
| **Requisito** | Cambia algo del negocio: un estado, una regla, un rol, un permiso. Afecta `Sistema_Equivalencias_Requisitos.md` y, si corresponde, `frontend-interfaces.md`. |
| **Desarrollo** | Cambia algo de la implementación técnica decidida en desarrollo: estructura de datos, flujo entre capas, estándar. Afecta `ARQUITECTURA_E3.md` o la sección 0 de `frontend-interfaces.md`. |

Lo que **no** va acá: corrección de erratas, typos o ajustes de formato. Esos son edits directos sin fila de cambio.

## Proceso

1. **Registrar** — agregar una fila en la tabla de abajo con `Estado: Propuesto`.
2. **Evaluar impacto** — definir qué documento y qué sección/requisito se ve afectado, y si algo ya implementado queda invalidado.
3. **Decidir** — `Aprobado` o `Rechazado`, con la motivación en la propia fila.
4. **Implementar** — editar el documento fuente afectado y agregar una fila nueva en su tabla de **Control de versiones** (la fila del cambio acá pasa a `Implementado` y se completa *Versión resultante*).
5. **Commitear** — mensaje con el prefijo correspondiente (`ADDED:`, `FIX:`, `REFACTOR:`) y el ID del cambio, por ejemplo: `FIX: CC-001 recarga selectiva de documentación`.

**Regla:** ningún commit que modifique un documento fuente se considera completo sin la fila correspondiente en la tabla de control de versiones de ese documento.

## Estados

```
Propuesto ──► Aprobado ──► Implementado
    │
    └────────► Rechazado
```

## Registro de cambios

| ID | Fecha | Tipo | Documento | Sección / RF | Descripción | Impacto | Estado | Versión resultante | Autor |
|----|-------|------|-----------|--------------|-------------|---------|--------|--------------------|-------|
| CC-001 | 2026-10-07 | Requisito | `Sistema_Equivalencias_Requisitos.md` · `frontend-interfaces.md` | RF-ADMIN-001, RF-ADMIN-003, §7.6 | Al otorgar acceso a una cuenta @usal, el admin debe indicar las facultades que la cuenta podrá observar y modificar | El alta hoy crea la cuenta con acceso básico y sin carreras, y la asignación posterior es por carrera; implica pasar a asignación por facultad con alcance observar/modificar | Propuesto | | lautarogomizelj |
| CC-002 | 2026-10-07 | Requisito | `Sistema_Equivalencias_Requisitos.md` | RF-AUTH-005, RF-AUTH-003 | Permiso para ingresar al sistema | El ingreso @usal hoy depende del rol y de la existencia en BD; haría falta un permiso de acceso explícito e independiente del rol | Propuesto | | lautarogomizelj |
| CC-003 | 2026-10-07 | Requisito | `Sistema_Equivalencias_Requisitos.md` · `frontend-interfaces.md` | RF-AUTH-005, §0.10, §7.5 | Permiso para visualizar los expedientes de una o muchas facultades | Contradice el acceso básico actual (ver todas las solicitudes en lectura) y el alcance por carrera; implica revisar la marca "SIN permission_level" (notas 7 y 26) | Propuesto | | lautarogomizelj |
| CC-004 | 2026-10-07 | Requisito | `Sistema_Equivalencias_Requisitos.md` · `frontend-interfaces.md` | RF-AUTH-005, RF-ASP-008, RF-PROC-005 | Permiso para descargar los expedientes que el usuario puede visualizar | La descarga hoy la tienen el aspirante y los participantes; requiere un permiso independiente del de visualización | Propuesto | | lautarogomizelj |
| CC-005 | 2026-10-07 | Requisito | `Sistema_Equivalencias_Requisitos.md` · `frontend-interfaces.md` | RF-AUTH-005, RF-ADMIN-003, RF-PROC-003 | Permiso para ser parte del proceso de expediente de una o muchas carreras dentro de las facultades asignadas | Cambia el alcance de carrera a facultad para "Tomar proceso" y separa visualización de participación; afecta tablas 3–4, queries de las secciones 6 y 8 | Propuesto | | lautarogomizelj |
