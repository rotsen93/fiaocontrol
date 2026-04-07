import Link from "next/link";
import { getCustomersWithBalance } from "@/app/actions/customers";
import { formatCurrencyDOP } from "@/lib/format";
import { MobileNav } from "@/components/mobile-nav";

export default async function ClientesPage() {
  const customers = await getCustomersWithBalance();

  return (
    <div className="min-h-screen bg-slate-50 p-4 pb-24 sm:p-8 sm:pb-8">
      <header className="mb-6 flex items-center justify-between">
        <div>
          <Link href="/dashboard" className="text-xs text-slate-400 hover:underline">← Dashboard</Link>
          <h1 className="mt-1 text-xl font-black text-slate-900">Clientes</h1>
        </div>
        <Link
          href="/clientes/nuevo"
          className="rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-slate-700"
        >
          + Nuevo
        </Link>
      </header>

      {customers.length === 0 ? (
        <p className="text-sm text-slate-400">No hay clientes aun. Agrega el primero.</p>
      ) : (
        <div className="space-y-2">
          {customers.map((c) => (
            <Link
              key={c.customer_id}
              href={`/clientes/${c.customer_id}`}
              className="flex items-center justify-between rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition hover:shadow-md"
            >
              <div>
                <p className="font-semibold text-slate-900">{c.name}</p>
                <p className="text-xs text-slate-400">{c.phone}{c.alias ? ` · ${c.alias}` : ""}</p>
              </div>
              <p className={`text-lg font-black ${Number(c.balance) > 0 ? "text-red-600" : "text-emerald-600"}`}>
                {formatCurrencyDOP(Number(c.balance))}
              </p>
            </Link>
          ))}
        </div>
      )}
      <MobileNav />
    </div>
  );
}
