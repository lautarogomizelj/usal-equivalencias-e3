export type TipoConsulta = 'DOCUMENTACION' | 'ESTADO' | 'CALCULOS' | 'FECHAS' | 'OTRO'

/** Mensaje del chat de una solicitud (contrato de la API §8.1), visto por quien pregunta. */
export interface Mensaje {
  id: number
  /** `rol` es el código del rol de quien escribió (`ASPIRANTE`, `ACAD`, `SECRE`). */
  autor: { tipo: 'USUARIO' | 'ASPIRANTE'; id: number; nombre: string; rol: string; avatar: string | null }
  contenido: string
  tipoConsulta: TipoConsulta
  adjunto: { nombre: string; urlDescarga: string } | null
  fechaEnvio: string
  /** Lo leyó alguien que no es quien lo escribió. */
  visto: boolean
  esMio: boolean
}
