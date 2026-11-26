import type { ComponentType } from 'react'
import type { SolicitudDetalle } from '@/types/solicitud'
import { AnalisisPreliminar } from './AnalisisPreliminar'
import { AvisoInasistencia, CoordinacionPorChat, MotivoDelRechazo } from './AvisosAspirante'
import { CargaDocumentacion } from './CargaDocumentacion'
import { DecisionInscripcion } from './DecisionInscripcion'
import { EleccionFecha } from './EleccionFecha'
import { FijarFecha } from './FijarFecha'
import { GestionPago } from './GestionPago'
import { AvisarLegalizado, IndicarLegalizacion } from './Legalizacion'
import { PagoAspirante } from './PagoAspirante'
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
  'REU-FEC-ACAD': AvisoInasistencia,
  'REU-CHAT': CoordinacionPorChat,
  'REU-PEND': ReunionPactada,
  'INSC-RECH': MotivoDelRechazo,
  'INSC-ACEP': IndicarLegalizacion,
  'LEG-PEND': AvisarLegalizado,
  'LEG-RECH': RecargaDocumentos,
  'CUPON-SOL': PagoAspirante,
  'CUPON-PAG': PagoAspirante,
  'PAG-CTRL': PagoAspirante,
  'PAG-REENV': PagoAspirante,
  'E3-INI': PagoAspirante,
}

/** Lo mismo para el personal: la acción que se ejecuta desde el banner, sin entrar a una pestaña. */
export const ACCIONES_EN_BANNER_PERSONAL: Record<string, ComponentType<VistaSolicitudProps>> = {
  'REU-CHAT': FijarFecha,
}

/** Estados en los que el detalle se abre en la pestaña Chat (frontend-interfaces.md §4.7.2 y §5.6.7). */
export const ESTADOS_CON_CHAT_ABIERTO = ['REU-CHAT']

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
  'ANAL-POST': DecisionInscripcion,
  'CUPON-SOL': GestionPago,
  'CUPON-PAG': GestionPago,
  'PAG-CTRL': GestionPago,
  'PAG-REENV': GestionPago,
  'E3-INI': GestionPago,
}
