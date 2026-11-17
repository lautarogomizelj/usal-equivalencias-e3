import { CloudUpload, LoaderCircle, RefreshCw } from 'lucide-react'
import { useState, type DragEvent } from 'react'
import { DOCUMENTOS } from '@/constants/documentos'

interface CargadorArchivoProps {
  /** Nombre accesible: qué documento se está cargando. */
  etiqueta: string
  subiendo?: boolean
  /** Botón chico para reemplazar un archivo ya cargado, en lugar de la zona de arrastre. */
  compacto?: boolean
  onArchivo: (archivo: File) => void
}

const CLASES_FOCO = 'has-focus-visible:outline-2 has-focus-visible:outline-offset-2 has-focus-visible:outline-focus'

/**
 * Cargador de archivos (frontend-interfaces.md §2.11): selector clásico o arrastrar y soltar.
 * La validación y la subida las hace quien lo usa, al recibir el archivo.
 */
export function CargadorArchivo({ etiqueta, subiendo = false, compacto = false, onArchivo }: CargadorArchivoProps) {
  const [encima, setEncima] = useState(false)

  function soltar(evento: DragEvent) {
    evento.preventDefault()
    setEncima(false)
    const archivo = evento.dataTransfer.files[0]
    if (archivo && !subiendo) onArchivo(archivo)
  }

  const selector = (
    <input
      type="file"
      className="sr-only"
      aria-label={etiqueta}
      accept={DOCUMENTOS.EXTENSIONES_PERMITIDAS.join(',')}
      disabled={subiendo}
      onChange={(evento) => {
        const archivo = evento.target.files?.[0]
        evento.target.value = ''
        if (archivo) onArchivo(archivo)
      }}
    />
  )

  if (compacto) {
    return (
      <label
        className={`inline-flex h-(--size-button-md) cursor-pointer items-center gap-2 rounded-md border border-primary bg-surface px-4 text-sm font-medium text-primary transition-colors hover:bg-secondary ${CLASES_FOCO}`}
      >
        <RefreshCw aria-hidden className="size-(--size-icon-sm)" />
        Reemplazar
        {selector}
      </label>
    )
  }

  return (
    <div>
      <label
        className={`flex min-h-(--size-cargador) cursor-pointer items-center justify-center gap-3 rounded-md border border-dashed px-4 py-2 transition-colors ${CLASES_FOCO} ${encima ? 'border-primary bg-secondary' : 'border-border-strong bg-surface hover:bg-surface-muted'}`}
        onDragOver={(evento) => {
          evento.preventDefault()
          setEncima(true)
        }}
        onDragLeave={() => setEncima(false)}
        onDrop={soltar}
      >
        {subiendo ? (
          <>
            <LoaderCircle aria-hidden className="size-(--size-icon-lg) animate-spin text-primary" />
            <span className="text-base font-medium text-text">Subiendo archivo…</span>
          </>
        ) : (
          <>
            <CloudUpload aria-hidden className="size-(--size-icon-xl) shrink-0 text-text-muted" />
            <span className="text-center">
              <span className="block text-base font-medium text-text">Hacé clic para seleccionar un archivo</span>
              <span className="block text-sm text-text-muted">o arrastralo aquí</span>
            </span>
          </>
        )}
        {selector}
      </label>
      <p className="mt-1 text-center text-xs text-text-muted">Formatos permitidos: PDF o JPG (máx. 10 MB)</p>
    </div>
  )
}
