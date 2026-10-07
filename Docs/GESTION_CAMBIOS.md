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
