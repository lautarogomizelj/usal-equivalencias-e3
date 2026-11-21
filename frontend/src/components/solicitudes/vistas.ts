import type { ComponentType } from 'react'
import type { SolicitudDetalle } from '@/types/solicitud'
import { AnalisisPreliminar } from './AnalisisPreliminar'
import { CargaDocumentacion } from './CargaDocumentacion'
import { EleccionFecha } from './EleccionFecha'
import { RecargaDocumentos } from './RecargaDocumentos'
import { ReunionPactada } from './ReunionPactada'
import { ReunionPersonal } from './ReunionPersonal'

export interface VistaSolicitudProps {
  detalle: SolicitudDetalle
  /** El backend devolvió la solicitud en su estado nuevo; `mensaje` es el aviso de éxito. */
  onActualizada: (detalle: SolicitudDetalle, mensaje: string) => void
  /** El backend rechazó la acción porque el estado ya no es el que se veía (§0.16.4). */
  onEstadoCambio: () => void
}

interface VistaEstado {
  titulo: string
  descripcion: string
  Vista: ComponentType<VistaSolicitudProps>
}

/**
 * Registro de interfaces del aspirante por código de estado (frontend-interfaces.md §0.16.2).
 * Un estado sin entrada muestra el detalle de solo lectura. Cada sprint suma las suyas.
 */
export const VISTAS_ASPIRANTE: Record<string, VistaEstado> = {
  'SOL-INI': {
    titulo: 'Carga de documentación',
    descripcion: 'Adjuntá la documentación requerida para tu solicitud de equivalencia.',
    Vista: CargaDocumentacion,
  },
}

/**
 * Estados en los que el aspirante actúa sin salir del detalle: su panel de acción va dentro del banner,
 * arriba de las pestañas (Anexo B, punto 18).
 */
export const ACCIONES_EN_BANNER: Record<string, ComponentType<VistaSolicitudProps>> = {
  'DOC-RECH': RecargaDocumentos,
  'REU-FEC-ASP-1': EleccionFecha,
  'REU-FEC-ASP-2': EleccionFecha,
  'REU-PEND': ReunionPactada,
}

/**
 * Panel del personal por código de estado (frontend-interfaces.md §5.6): va arriba de los documentos, en la
 * pestaña "Análisis de solicitud". Cada panel muestra sus acciones solo a quien las tiene disponibles.
 */
export const VISTAS_PERSONAL: Record<string, ComponentType<VistaSolicitudProps>> = {
  'ANAL-PEND': AnalisisPreliminar,
  'REU-FEC-ASP-1': ReunionPersonal,
  'REU-FEC-ASP-2': ReunionPersonal,
  'REU-FEC-ACAD': ReunionPersonal,
  'REU-CHAT': ReunionPersonal,
  'REU-PEND': ReunionPersonal,
}
