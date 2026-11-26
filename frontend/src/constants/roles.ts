/** Roles soportados (frontend-interfaces.md §0.10.2). */
export const ROLES = {
  ASPIRANTE: 'aspirante',
  ACADEMICO: 'academico',
  SECRETARIA: 'secretaria',
  ADMIN: 'admin',
  REVISOR: 'revisor',
  OBSERVADOR: 'observador',
} as const

/** Nombre a mostrar de cada rol, por el código que envía el backend (contrato de la API §2.1). */
export const NOMBRE_ROL: Record<string, string> = {
  ASPIRANTE: 'Aspirante',
  ACAD: 'Académico',
  SECRE: 'Secretaría',
  ADMIN: 'Admin',
  REVISOR: 'Revisor',
}

/** Roles que el Admin puede darle a una cuenta del personal (RF-ADMIN-001). */
export const ROLES_DEL_PERSONAL = ['ACAD', 'SECRE', 'REVISOR']
