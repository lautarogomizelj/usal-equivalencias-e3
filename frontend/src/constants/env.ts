/** Variables de entorno. Definirlas en `.env` (ver `.env.example`). */
export const ENV = {
  API_URL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
  USAR_MOCKS: import.meta.env.VITE_USE_MOCKS === 'true',
} as const
