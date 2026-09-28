import type { ROLES } from '@/constants/roles'

export type Rol = (typeof ROLES)[keyof typeof ROLES]
