import type { ReactNode } from 'react'
import { Migas } from '@/components/ui'
import { FotoEdificio } from './FotoEdificio'

interface EncabezadoPaginaProps {
  titulo: string
  descripcion: string
  /** Pantalla a la que se vuelve; se muestra en la barra superior. */
  migas?: { origen: { texto: string; ruta: string }; actual: string }
  /** Foto más baja y solo en pantallas anchas, para títulos largos o contenido de ancho completo debajo. */
  fotoBaja?: boolean
  /** Contenido al lado del título, por ejemplo el badge de estado. */
  children?: ReactNode
}

/** Título, descripción y foto de las pantallas del área privada. Va primero dentro de `<main>`. */
export function EncabezadoPagina({ titulo, descripcion, migas, fotoBaja = false, children }: EncabezadoPaginaProps) {
  return (
    <>
      <FotoEdificio
        className={`pointer-events-none absolute top-0 right-0 hidden max-w-none ${fotoBaja ? 'h-(--size-privado-foto-baja) xl:block' : 'h-(--size-privado-foto-alto) lg:block'}`}
      />

      {migas && (
        <Migas {...migas} className="relative mb-4 lg:absolute lg:top-0 lg:mb-0 lg:h-(--size-privado-barra)" />
      )}

      <div className="relative flex flex-wrap items-center gap-x-6 gap-y-2 lg:max-w-(--size-privado-titulo)">
        <h1 className="text-4xl lg:text-6xl">{titulo}</h1>
        {children}
      </div>
      <p className="relative mt-2 text-lg text-text-muted lg:mt-0 lg:text-xl">{descripcion}</p>
    </>
  )
}
