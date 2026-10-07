# E3 · Frontend

Frontend del Sistema de Tramitación de Equivalencias Electrónicas (E3).
Pantallas y reglas: [`./frontend-interfaces.md`](./frontend-interfaces.md) · Requisitos: [`./Sistema_Equivalencias_Requisitos.md`](./Sistema_Equivalencias_Requisitos.md) · Stack, puertos y git: [`../README.md`](../README.md).

## Stack

- [Vite](https://vite.dev) + React + TypeScript
- [Tailwind CSS v4](https://tailwindcss.com) con los Design Tokens del proyecto
- [React Router](https://reactrouter.com)
- [Lucide](https://lucide.dev) (`lucide-react`) como **única** librería de íconos (§0.3.7)
- Estado global: Context API de React (`src/store/`)

## Cómo correrlo

```bash
cd frontend
cp .env.example .env   # URL del backend
npm install
npm run dev            # http://localhost:5173
```

Otros scripts: `npm run build`, `npm run preview`, `npm run lint`.

## Estructura (§0.2.4)

```text
src/
├── assets/        imágenes y archivos estáticos
├── components/
│   └── ui/        componentes base compartidos (Button, Input, Modal, Table...)
├── constants/     rutas, roles, límites de archivos, reglas de auth, env
├── hooks/         hooks reutilizables
├── layouts/       layouts compartidos (público, dashboard, detalle...)
├── pages/         una carpeta por área/rol (public, aspirante, academico...)
├── router/        definición de rutas
├── services/      comunicación con el backend (api/client.ts es el único que usa fetch)
├── store/         estado global (Context API)
├── styles/        estilos globales
├── tokens/        Design Tokens (colores, espaciado, radios, sombras, tipografía...)
├── types/         tipos y DTOs compartidos
├── utils/         utilidades puras
└── validations/   validaciones y mensajes de error compartidos
```

Importar con el alias `@/` (ej: `import { Button } from '@/components/ui'`).

## Reglas rápidas

- **Nada hardcodeado:** colores, espaciados, radios, sombras y tamaños salen de `src/tokens/`.
  Usar clases como `bg-primary`, `text-danger`, `rounded-md`, `h-(--size-input)`, `z-(--z-modal)`.
  Nada de valores arbitrarios (`p-[13px]`, `bg-[#ff0000]`).
- **Escala de espaciado:** solo `1, 2, 3, 4, 6, 8, 12, 16` (= 4 · 8 · 12 · 16 · 24 · 32 · 48 · 64 px).
- **Sin estilos inline** (`style={{...}}`).
- **Sin `fetch` en componentes:** crear un servicio en `src/services/` que use `api` de `services/api/client.ts`.
  Los errores llegan como `ApiError` con `kind` (`unauthorized`, `forbidden`, `network`...).
- **Estados por código, nunca por nombre** (`estado.codigo === 'DOC-CAR'`); el catálogo viene del backend.
- **Validaciones** en `src/validations/`; el backend sigue siendo la fuente de verdad.
- **Acciones críticas** con doble confirmación (§12).
- Cada pantalla contempla estados de carga, vacío, error y éxito (§0.5).

## Pendiente

- Los colores de `tokens/colors.css` son **provisorios** hasta tener el Design System definitivo.
- Falta definir con backend el contrato de la API (rutas, DTOs y formato de errores en `types/api.ts`).
