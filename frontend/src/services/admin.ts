/** Administración (contrato de la API §5 y §9). Solo Admin General. */
import { api } from '@/services/api/client'
import type {
  Alertas,
  AsignacionResponsables,
  CuentaBloqueada,
  DatosCarrera,
  DatosUsuario,
  FiltrosUsuarios,
  UsuarioAdmin,
} from '@/types/admin'
import type { Page } from '@/types/api'
import type { Carrera } from '@/types/carrera'
import type { SolicitudDetalle } from '@/types/solicitud'
import { consulta } from '@/utils/consulta'

export const admin = {
  usuarios: (page: number, filtros: FiltrosUsuarios = {}, signal?: AbortSignal) =>
    api.get<Page<UsuarioAdmin>>(`/admin/usuarios?${consulta(page, filtros)}`, { signal }),
  usuario: (id: number) => api.get<UsuarioAdmin>(`/admin/usuarios/${id}`),
  crearUsuario: (datos: DatosUsuario) => api.post<UsuarioAdmin>('/admin/usuarios', datos),
  /** Cada dato es opcional: solo viaja lo que cambia. */
  editarUsuario: (id: number, cambios: Partial<Omit<DatosUsuario, 'email'>> & { activo?: boolean }) =>
    api.patch<UsuarioAdmin>(`/admin/usuarios/${id}`, cambios),
  asignarCarreras: (id: number, carreraIds: number[]) =>
    api.post<UsuarioAdmin>(`/admin/usuarios/${id}/permisos/carreras`, { carreraIds }),
  /** Con procesos activos responde 409 `PROCESO_ACTIVO`, salvo que vaya `forzar`. */
  revocarCarrera: (permisoId: number, forzar: boolean, comentario: string) =>
    api.post<void>(`/admin/permisos/carreras/${permisoId}/revocar`, { forzar, comentario }),
  revocarSolicitud: (permisoId: number, comentario: string) =>
    api.post<void>(`/admin/permisos/solicitudes/${permisoId}/revocar`, { comentario }),
  asignarResponsables: (solicitudId: number, datos: AsignacionResponsables) =>
    api.post<SolicitudDetalle>(`/admin/solicitudes/${solicitudId}/responsables`, datos),
  alertas: (signal?: AbortSignal) => api.get<Alertas>('/admin/alertas', { signal }),
  desbloquear: ({ tipo, id }: Pick<CuentaBloqueada, 'tipo' | 'id'>) =>
    api.post<void>(`/admin/cuentas/${tipo}/${id}/desbloquear`),
  /** Con las inactivas, que el resto de los roles no ve. */
  carreras: (signal?: AbortSignal) => api.get<Carrera[]>('/carreras?activo=todas', { signal }),
  crearCarrera: (datos: DatosCarrera) => api.post<Carrera>('/carreras', datos),
  editarCarrera: (id: number, datos: DatosCarrera) => api.put<Carrera>(`/carreras/${id}`, datos),
  activarCarrera: (id: number, activo: boolean) => api.patch<Carrera>(`/carreras/${id}/activo`, { activo }),
}
