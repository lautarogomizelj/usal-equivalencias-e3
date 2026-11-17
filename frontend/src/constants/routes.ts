/** Rutas de la aplicación. Toda navegación debe usar estas constantes. */
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  LOGIN_PERSONAL: '/login-personal',
  LOGIN_ADMIN: '/login-admin',
  REGISTRO: '/registro',
  VALIDAR_CODIGO: '/validar-codigo',
  RECUPERAR_PASSWORD: '/recuperar-password',
  DASHBOARD: '/dashboard',
  MIS_SOLICITUDES: '/solicitudes',
  NUEVA_SOLICITUD: '/nueva-solicitud',
  SOLICITUD: '/solicitudes/:id',
  PERFIL: '/perfil',
  AYUDA: '/ayuda',
  DEV_COMPONENTES: '/dev/componentes',
} as const
