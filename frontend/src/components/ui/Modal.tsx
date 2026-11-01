import { X, type LucideIcon } from 'lucide-react'
import { useEffect, useRef, type ReactNode } from 'react'

interface ModalProps {
  abierto: boolean
  titulo: string
  descripcion?: string
  Icono?: LucideIcon
  onCerrar: () => void
  children?: ReactNode
  acciones?: ReactNode
}

/** Modal base sobre `<dialog>`: aporta el foco atrapado y el cierre con Escape (§0.6). */
export function Modal({ abierto, titulo, descripcion, Icono, onCerrar, children, acciones }: ModalProps) {
  const dialogo = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    if (abierto) dialogo.current?.showModal()
    else dialogo.current?.close()
  }, [abierto])

  return (
    <dialog
      ref={dialogo}
      aria-labelledby="modal-titulo"
      onCancel={(evento) => {
        evento.preventDefault()
        onCerrar()
      }}
      className="m-auto w-full max-w-(--size-modal-md) rounded-xl bg-surface p-6 text-text shadow-lg backdrop:bg-overlay"
    >
      <header className="flex items-start gap-4">
        {Icono && (
          <span className="flex size-(--size-circulo-icono) shrink-0 items-center justify-center rounded-full bg-secondary text-primary">
            <Icono aria-hidden className="size-(--size-icon-lg)" />
          </span>
        )}
        <div className="flex-1">
          <h2 id="modal-titulo" className="text-2xl">
            {titulo}
          </h2>
          {descripcion && <p className="mt-1 text-sm text-text-muted">{descripcion}</p>}
        </div>
        <button
          type="button"
          aria-label="Cerrar"
          className="text-text-muted hover:text-primary"
          onClick={onCerrar}
        >
          <X aria-hidden className="size-(--size-icon-md)" />
        </button>
      </header>
      {children && <div className="mt-6">{children}</div>}
      {acciones && <footer className="mt-6 flex flex-wrap justify-end gap-3">{acciones}</footer>}
    </dialog>
  )
}
