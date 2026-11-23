import { FilePen, RefreshCw } from 'lucide-react'
import { useState } from 'react'
import { Button, Modal, Seccion } from '@/components/ui'
import { NOMBRE_ANALISIS } from '@/constants/documentos'
import { useAccion } from '@/hooks/useAccion'
import { acciones } from '@/services/acciones'
import { solicitudes } from '@/services/solicitudes'
import type { TipoAnalisis } from '@/types/solicitud'
import { archivo as validarArchivo } from '@/validations'
import { DocumentoAnalisis } from './AnalisisPreliminar'
import type { VistaSolicitudProps } from './vistas'

const TIPOS = Object.keys(NOMBRE_ANALISIS) as TipoAnalisis[]
/** Texto corto: el botón tiene que entrar en una línea también en celular. */
const BOTON: Record<TipoAnalisis, string> = {
  MATRIZ_EQUIVALENCIAS: 'Actualizar la matriz',
  HORARIO_CURSADA: 'Actualizar el horario',
}

/**
 * Actualización de la matriz de equivalencias o del horario de cursada, `ACAD-ANAL-POST` (frontend-interfaces.md
 * §5.6.9): crea una versión nueva, que reemplaza entera a la anterior. El estado no cambia.
 */
export function ActualizarAnalisis({ detalle, onActualizada, onEstadoCambio }: VistaSolicitudProps) {
  const [tipo, setTipo] = useState<TipoAnalisis | null>(null)
  const [archivo, setArchivo] = useState<File | null>(null)
  const [notas, setNotas] = useState('')
  const [errorCampo, setErrorCampo] = useState<string | null>(null)
  const { procesando, error, setError, ejecutar } = useAccion(onEstadoCambio)

  if (!detalle.accionesDisponibles.includes('ACTUALIZAR_ANALISIS')) return null

  function abrir(cual: TipoAnalisis) {
    setArchivo(null)
    setNotas('')
    setErrorCampo(null)
    setError(null)
    setTipo(cual)
  }

  function elegirArchivo(nuevo: File | null) {
    const invalido = nuevo && validarArchivo(nuevo)
    if (invalido) return setErrorCampo(invalido)
    setErrorCampo(null)
    setArchivo(nuevo)
  }

  function guardar(cual: TipoAnalisis) {
    if (!archivo && !notas.trim()) return setErrorCampo('Cargá un archivo, escribí notas o las dos cosas.')
    return ejecutar(async () => {
      await acciones.actualizarAnalisis(detalle.id, cual, archivo, notas)
      setTipo(null)
      // La solicitud no cambió de estado: se vuelve a pedir para que la documentación académica muestre lo nuevo.
      onActualizada(
        await solicitudes.obtener(detalle.id),
        `Se actualizó: ${NOMBRE_ANALISIS[cual]}. Le avisamos al aspirante por mail.`,
      )
    })
  }

  return (
    <Seccion
      Icono={FilePen}
      titulo="Actualizar el análisis"
      descripcion="Si después de la reunión hace falta, cargá una versión nueva de la matriz o del horario de cursada antes de decidir la inscripción."
    >
      <div className="flex flex-wrap gap-3">
        {TIPOS.map((cual) => (
          <Button key={cual} variante="outline" tamanio="lg" onClick={() => abrir(cual)}>
            <RefreshCw aria-hidden className="size-(--size-icon-md)" />
            {BOTON[cual]}
          </Button>
        ))}
      </div>

      <Modal
        grande
        abierto={tipo !== null}
        titulo="Actualizar el análisis"
        descripcion="La versión nueva reemplaza entera a la anterior: lo que no cargues acá deja de verse. Se le avisa al aspirante por mail."
        Icono={FilePen}
        onCerrar={() => setTipo(null)}
        acciones={
          <>
            <Button variante="outline" disabled={procesando} onClick={() => setTipo(null)}>
              Cancelar
            </Button>
            <Button disabled={procesando} onClick={() => tipo && guardar(tipo)}>
              {procesando ? 'Guardando…' : 'Guardar versión nueva'}
            </Button>
          </>
        }
      >
        {tipo && (
          <div className="formulario-compacto">
            <DocumentoAnalisis
              nombre={NOMBRE_ANALISIS[tipo]}
              archivo={archivo}
              notas={notas}
              error={errorCampo ?? error}
              onArchivo={elegirArchivo}
              onNotas={(nuevas) => {
                setNotas(nuevas)
                setErrorCampo(null)
              }}
            />
          </div>
        )}
      </Modal>
    </Seccion>
  )
}
