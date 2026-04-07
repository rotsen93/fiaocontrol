import Link from "next/link";

export function MobileNav() {
  return (
    <nav className="fixed inset-x-0 bottom-0 z-40 border-t border-slate-200 bg-white/95 px-3 py-2 backdrop-blur sm:hidden">
      <div className="mx-auto grid max-w-md grid-cols-3 gap-2">
        <Link
          href="/dashboard"
          className="rounded-xl bg-slate-100 px-3 py-2 text-center text-xs font-bold text-slate-700"
        >
          Dashboard
        </Link>
        <Link
          href="/clientes"
          className="rounded-xl bg-slate-100 px-3 py-2 text-center text-xs font-bold text-slate-700"
        >
          Clientes
        </Link>
        <Link
          href="/clientes/nuevo"
          className="rounded-xl bg-slate-900 px-3 py-2 text-center text-xs font-bold text-white"
        >
          + Nuevo
        </Link>
      </div>
    </nav>
  );
}
