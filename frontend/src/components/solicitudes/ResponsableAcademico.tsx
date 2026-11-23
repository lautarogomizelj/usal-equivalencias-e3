import { User } from 'lucide-react'
import type { Responsable } from '@/types/solicitud'

/** Con quién tiene que coordinar el aspirante: va dentro del banner del estado. */
export function ResponsableAcademico({ responsable }: { responsable: Responsable }) {
  return (
    <p className="flex min-w-0 grow basis-64 items-center gap-3 rounded-md bg-surface p-3 text-base text-text">
      <User aria-hidden className="size-(--size-icon-lg) shrink-0 text-primary" />
      <span className="font-medium">Responsable académico</span>
      <span className="min-w-0 border-l border-border pl-3">
        {responsable.nombre}
        <a href={`mailto:${responsable.email}`} className="block truncate text-primary underline">
          {responsable.email}
        </a>
      </span>
    </p>
  )
}
