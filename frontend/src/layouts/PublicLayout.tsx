import { Outlet } from 'react-router'

/** Layout de las pantallas públicas (frontend-interfaces.md §3). */
export function PublicLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <header className="flex h-(--size-topbar) items-center border-b border-border bg-surface px-6">
        <span className="text-lg font-semibold text-primary">E3 · Equivalencias USAL</span>
      </header>
      <main className="flex flex-1 items-center justify-center p-4 sm:p-8">
        <Outlet />
      </main>
    </div>
  )
}
