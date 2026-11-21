/**
 * Acciones del trámite (contrato de la API §7). Las que cambian el estado envían el que el usuario tenía en
 * pantalla; tomar el proceso, darse de baja, agregar un faltante y cargar el enlace de Meet no lo cambian.
 */
import { api } from '@/services/api/client'
import type {
  Documento,
  Marca,
  PropuestaAnalisis,
  ReunionInfo,
  SolicitudDetalle,
  TipoDocumento,
} from '@/types/solicitud'

const horarios = (inicios: string[]) => inicios.map((inicio) => ({ inicio }))

export const acciones = {
  enviarDocumentacion: (id: number, estadoEsperado: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/documentacion/enviar`, { estadoEsperado }),
  cancelarSolicitud: (id: number, estadoEsperado: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/cancelar`, { estadoEsperado }),
  tomarProceso: (id: number) => api.post<SolicitudDetalle>(`/solicitudes/${id}/responsables/tomar`),
  darseDeBaja: (id: number) => api.post<SolicitudDetalle>(`/solicitudes/${id}/responsables/baja`),
  /** Guarda las marcas sin cambiar el estado y devuelve los documentos vigentes. */
  guardarMarcas: (id: number, estadoEsperado: string, marcas: Marca[]) =>
    api.put<Documento[]>(`/solicitudes/${id}/documentos/marcas`, { estadoEsperado, marcas }),
  agregarFaltante: (id: number, tipo: TipoDocumento, descripcion: string) =>
    api.post<Documento>(`/solicitudes/${id}/documentos/faltantes`, { tipo, descripcion }),
  aceptarDocumentacion: (id: number, estadoEsperado: string, marcas: Marca[]) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/documentacion/aceptar`, { estadoEsperado, marcas }),
  rechazarDocumentacion: (id: number, estadoEsperado: string, comentario: string, marcas: Marca[]) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/documentacion/rechazar`, { estadoEsperado, comentario, marcas }),
  /** Matriz, horario de cursada y fechas de reunión en un solo envío; `datos` viaja como JSON (contrato §7.3). */
  proponerFechas: (id: number, estadoEsperado: string, propuesta: PropuestaAnalisis) => {
    const cuerpo = new FormData()
    if (propuesta.matrizArchivo) cuerpo.append('matrizArchivo', propuesta.matrizArchivo)
    if (propuesta.matrizNotas.trim()) cuerpo.append('matrizNotas', propuesta.matrizNotas.trim())
    if (propuesta.horarioArchivo) cuerpo.append('horarioArchivo', propuesta.horarioArchivo)
    if (propuesta.horarioNotas.trim()) cuerpo.append('horarioNotas', propuesta.horarioNotas.trim())
    const datos = { estadoEsperado, horarios: horarios(propuesta.horarios) }
    cuerpo.append('datos', new Blob([JSON.stringify(datos)], { type: 'application/json' }))
    return api.post<SolicitudDetalle>(`/solicitudes/${id}/analisis/propuesta`, cuerpo)
  },
  proponerSegundaRonda: (id: number, estadoEsperado: string, inicios: string[]) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/reunion/segunda-ronda`, { estadoEsperado, horarios: horarios(inicios) }),
  elegirHorario: (id: number, estadoEsperado: string, horarioId: number) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/reunion/elegir-horario`, { estadoEsperado, horarioId }),
  solicitarOtrasFechas: (id: number, estadoEsperado: string, motivo: string, disponibilidad: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/reunion/solicitar-otras-fechas`, {
      estadoEsperado,
      motivo,
      disponibilidad,
    }),
  rechazarSegundaRonda: (id: number, estadoEsperado: string, motivo: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/reunion/rechazar-ronda`, { estadoEsperado, motivo }),
  /** No cambia el estado: devuelve la reunión con el enlace cargado. */
  cargarEnlaceMeet: (id: number, enlaceMeet: string) =>
    api.put<ReunionInfo>(`/solicitudes/${id}/reunion/enlace`, { enlaceMeet }),
}
