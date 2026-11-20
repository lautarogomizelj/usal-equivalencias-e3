import { NOMBRE_TIPO_DOCUMENTO } from '@/constants/documentos'
import type { Documento } from '@/types/solicitud'

/** Cómo se nombra un documento en pantalla: su tipo y, si la tiene, la descripción ("Programa de la asignatura: Física I"). */
export function tituloDocumento({ tipo, descripcion }: Pick<Documento, 'tipo' | 'descripcion'>) {
  return descripcion ? `${NOMBRE_TIPO_DOCUMENTO[tipo]}: ${descripcion}` : NOMBRE_TIPO_DOCUMENTO[tipo]
}
