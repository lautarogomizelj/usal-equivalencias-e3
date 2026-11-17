const NAVEGADORES: [RegExp, string][] = [
  [/Edg\//, 'Edge'],
  [/Firefox\//, 'Firefox'],
  [/Chrome\//, 'Chrome'],
  [/Safari\//, 'Safari'],
]

const SISTEMAS: [RegExp, string][] = [
  [/Android/, 'Android'],
  [/iPhone|iPad/, 'iOS'],
  [/Windows/, 'Windows'],
  [/Mac OS/, 'macOS'],
  [/Linux/, 'Linux'],
]

/** Resume el `userAgent` de una sesión como "Chrome en Windows". */
export function describirDispositivo(userAgent: string | null) {
  const buscar = (lista: [RegExp, string][]) => lista.find(([patron]) => patron.test(userAgent ?? ''))?.[1]
  return [buscar(NAVEGADORES), buscar(SISTEMAS)].filter(Boolean).join(' en ') || 'Dispositivo desconocido'
}
