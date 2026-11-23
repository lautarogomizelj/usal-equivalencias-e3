/**
 * Acciones del trámite (contrato de la API §7). Las que cambian el estado envían el que el usuario tenía en
 * pantalla; tomar el proceso, darse de baja, agregar un faltante, cargar el enlace de Meet y actualizar el
 * análisis no lo cambian.
 */
import { api } from '@/services/api/client'
import type {
  Analisis,
  Documento,
  Marca,
  PropuestaAnalisis,
  ReunionInfo,
  SolicitudDetalle,
  TipoAnalisis,
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
  /** `inicio` es la fecha que pautaron por chat, en ISO 8601. */
  fijarFecha: (id: number, estadoEsperado: string, inicio: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/reunion/fijar-fecha`, { estadoEsperado, inicio }),
  marcarNoAsistio: (id: number, estadoEsperado: string, comentario: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/reunion/no-asistio`, { estadoEsperado, comentario }),
  finalizarReunion: (id: number, estadoEsperado: string, notas: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/reunion/finalizar`, { estadoEsperado, notas }),
  /** No cambia el estado: la versión nueva reemplaza a la anterior y devuelve los dos documentos vigentes. */
  actualizarAnalisis: (id: number, tipo: TipoAnalisis, archivo: File | null, notas: string) => {
    const cuerpo = new FormData()
    cuerpo.append('tipo', tipo)
    if (archivo) cuerpo.append('archivo', archivo)
    if (notas.trim()) cuerpo.append('notas', notas.trim())
    return api.post<Analisis[]>(`/solicitudes/${id}/analisis`, cuerpo)
  },
  aceptarInscripcion: (id: number, estadoEsperado: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/inscripcion/aceptar`, { estadoEsperado }),
  rechazarInscripcion: (id: number, estadoEsperado: string, comentario: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/inscripcion/rechazar`, { estadoEsperado, comentario }),
  indicarLegalizacion: (id: number, estadoEsperado: string, legalizados: boolean) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/legalizacion/indicar`, { estadoEsperado, legalizados }),
  avisarLegalizado: (id: number, estadoEsperado: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/legalizacion/listo`, { estadoEsperado }),
  /** Con la aceptación va el informe de materias: cuántas se reconocen y las observaciones. */
  aceptarLegalizados: (
    id: number,
    estadoEsperado: string,
    marcas: Marca[],
    materiasReconocidas: number,
    observaciones: string,
  ) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/legalizacion/aceptar`, {
      estadoEsperado,
      marcas,
      materiasReconocidas,
      observaciones,
    }),
  rechazarLegalizados: (id: number, estadoEsperado: string, comentario: string, marcas: Marca[]) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/legalizacion/rechazar`, { estadoEsperado, comentario, marcas }),
  enviarCupon: (id: number, estadoEsperado: string, monto: number) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/pago/cupon`, { estadoEsperado, monto }),
  enviarComprobante: (id: number, estadoEsperado: string, archivo: File) => {
    const cuerpo = new FormData()
    cuerpo.append('estadoEsperado', estadoEsperado)
    cuerpo.append('archivo', archivo)
    return api.post<SolicitudDetalle>(`/solicitudes/${id}/pago/comprobante`, cuerpo)
  },
  aceptarPago: (id: number, estadoEsperado: string, comentario: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/pago/aceptar`, { estadoEsperado, comentario }),
  rechazarPago: (id: number, estadoEsperado: string, comentario: string) =>
    api.post<SolicitudDetalle>(`/solicitudes/${id}/pago/rechazar`, { estadoEsperado, comentario }),
}
