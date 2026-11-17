import { IdCard, Mail, Pencil, Phone, User, type LucideIcon } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Aviso, Button, Input, Seccion } from '@/components/ui'
import { NOMBRE_ROL } from '@/constants/roles'
import { perfil as servicioPerfil } from '@/services/perfil'
import type { Perfil } from '@/types/perfil'
import { erroresPorCampo, mensajeDeError } from '@/utils/errores'
import { iniciales, nombreCompleto } from '@/utils/personas'
import { requerido, telefono as validarTelefono } from '@/validations'

interface DatosPersonalesProps {
  perfil: Perfil
  onGuardado: (perfil: Perfil) => void
}

function Dato({ Icono, etiqueta, valor }: { Icono: LucideIcon; etiqueta: string; valor: string | null }) {
  return (
    <div className="flex items-center gap-3 py-2">
      <Icono aria-hidden className="size-(--size-icon-md) shrink-0 text-primary" />
      <dt className="w-1/3 shrink-0 text-base text-text-muted">{etiqueta}</dt>
      <dd className="min-w-0 text-base break-words text-text">{valor || '–'}</dd>
    </div>
  )
}

/** Datos del perfil: se editan solo los campos que el backend marca en `camposEditables` (RF-AUTH-007). */
export function DatosPersonales({ perfil, onGuardado }: DatosPersonalesProps) {
  const [editando, setEditando] = useState(false)
  const [datos, setDatos] = useState({ nombre: '', apellido: '', telefono: '' })
  const [errores, setErrores] = useState<Record<string, string | null>>({})
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)
  const puede = (campo: string) => perfil.camposEditables.includes(campo)
  const editable = ['nombre', 'apellido', 'telefono'].some(puede)

  function editar() {
    setDatos({ nombre: perfil.nombre, apellido: perfil.apellido, telefono: perfil.telefono ?? '' })
    setErrores({})
    setErrorGeneral(null)
    setEditando(true)
  }

  async function guardar(evento: FormEvent) {
    evento.preventDefault()
    const telefono = datos.telefono.trim()
    const validacion = {
      nombre: puede('nombre') ? requerido(datos.nombre) : null,
      apellido: puede('apellido') ? requerido(datos.apellido) : null,
      telefono: telefono || perfil.telefono ? (requerido(telefono) ?? validarTelefono(telefono)) : null,
    }
    setErrores(validacion)
    if (Object.values(validacion).some(Boolean)) return

    const cambios = Object.fromEntries(
      (['nombre', 'apellido', 'telefono'] as const)
        .map((campo) => [campo, datos[campo].trim()] as const)
        .filter(([campo, valor]) => puede(campo) && valor !== (perfil[campo] ?? '')),
    )
    if (Object.keys(cambios).length === 0) return setEditando(false)

    setGuardando(true)
    setErrorGeneral(null)
    try {
      onGuardado(await servicioPerfil.editar(cambios))
      setEditando(false)
    } catch (error) {
      const porCampo = erroresPorCampo(error)
      setErrores(porCampo)
      if (Object.keys(porCampo).length === 0) setErrorGeneral(mensajeDeError(error))
    } finally {
      setGuardando(false)
    }
  }

  return (
    <Seccion
      Icono={User}
      titulo="Información personal"
      descripcion={perfil.esOAuth ? 'Tu nombre y tu email vienen de tu cuenta de Google.' : 'Tus datos de contacto.'}
    >
      <div className="flex flex-wrap items-center gap-4 border-b border-border pb-4">
        <span
          aria-hidden
          className="flex size-(--size-privado-indicador) shrink-0 items-center justify-center rounded-full bg-secondary text-3xl font-semibold text-primary"
        >
          {iniciales(perfil)}
        </span>
        <p className="min-w-0 flex-1">
          <span className="block font-serif text-3xl break-words text-heading">{nombreCompleto(perfil)}</span>
          <span className="block text-lg text-text-muted">{NOMBRE_ROL[perfil.rol]}</span>
        </p>
        {editable && !editando && (
          <Button variante="outline" onClick={editar}>
            <Pencil aria-hidden className="size-(--size-icon-sm)" />
            Editar
          </Button>
        )}
      </div>

      {editando ? (
        <form className="mt-4 grid gap-4 md:grid-cols-2" noValidate onSubmit={guardar}>
          <Input
            required
            etiqueta="Nombre"
            autoComplete="given-name"
            disabled={!puede('nombre')}
            value={datos.nombre}
            error={errores.nombre}
            onChange={(evento) => setDatos({ ...datos, nombre: evento.target.value })}
          />
          <Input
            required
            etiqueta="Apellido"
            autoComplete="family-name"
            disabled={!puede('apellido')}
            value={datos.apellido}
            error={errores.apellido}
            onChange={(evento) => setDatos({ ...datos, apellido: evento.target.value })}
          />
          <Input etiqueta="Email" value={perfil.email} disabled readOnly />
          <Input
            etiqueta="Teléfono"
            type="tel"
            autoComplete="tel"
            placeholder="Ej.: +54 11 5555-0000"
            value={datos.telefono}
            error={errores.telefono}
            onChange={(evento) => setDatos({ ...datos, telefono: evento.target.value })}
          />
          {errorGeneral && (
            <div className="md:col-span-2">
              <Aviso tipo="error" mensaje={errorGeneral} />
            </div>
          )}
          <div className="flex flex-wrap justify-end gap-3 md:col-span-2">
            <Button variante="outline" disabled={guardando} onClick={() => setEditando(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={guardando}>
              {guardando ? 'Guardando…' : 'Guardar cambios'}
            </Button>
          </div>
        </form>
      ) : (
        <dl className="mt-2 divide-y divide-border">
          <Dato Icono={User} etiqueta="Nombre" valor={perfil.nombre} />
          <Dato Icono={User} etiqueta="Apellido" valor={perfil.apellido} />
          <Dato Icono={Mail} etiqueta="Email" valor={perfil.email} />
          <Dato Icono={Phone} etiqueta="Teléfono" valor={perfil.telefono} />
          {perfil.dni && <Dato Icono={IdCard} etiqueta="DNI" valor={perfil.dni} />}
        </dl>
      )}
    </Seccion>
  )
}
