import Link from "next/link";
import { getDashboardTotals } from "@/app/actions/ledger";
import { getCustomersWithBalance } from "@/app/actions/customers";
import { formatCurrencyDOP } from "@/lib/format";
import { QuickEntryForm } from "./QuickEntryForm";
import { MobileNav } from "@/components/mobile-nav";
import { LogoutButton } from "@/components/logout-button";

export default async function DashboardPage() {
  const [totals, customers] = await Promise.all([
    getDashboardTotals(),
    getCustomersWithBalance(),
  ]);

  const topDeudores = [...customers]
    .filter((c) => Number(c.balance) > 0)
    .sort((a, b) => Number(b.balance) - Number(a.balance))
    .slice(0, 10);

  return (
    <div className="min-h-screen bg-slate-50 p-4 pb-24 sm:p-8 sm:pb-8">
      <header className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-slate-900">FiaoControl</h1>
          <p className="text-sm text-slate-500">Cuaderno digital de fiaos</p>
        </div>
        <div className="flex items-center gap-2">
          <Link
            href="/clientes/nuevo"
            className="rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-slate-700"
          >
            + Cliente
          </Link>
          <LogoutButton />
        </div>
      </header>

      <div className="mb-6 grid gap-4 sm:grid-cols-2">
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <p className="text-xs font-semibold tracking-widest text-slate-500 uppercase">Deuda total</p>
          <p className="mt-1 text-3xl font-black text-red-600">{formatCurrencyDOP(totals.totalDebt)}</p>
        </div>
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <p className="text-xs font-semibold tracking-widest text-slate-500 uppercase">Clientes</p>
          <p className="mt-1 text-3xl font-black text-slate-900">{totals.customerCount}</p>
        </div>
      </div>

      <QuickEntryForm
        customers={customers.map((c) => ({
          customer_id: c.customer_id,
          name: c.name,
          alias: c.alias,
        }))}
      />

      <section>
        <h2 className="mb-3 text-sm font-bold text-slate-700 uppercase tracking-widest">
          Top deudores
        </h2>
        {topDeudores.length === 0 ? (
          <p className="text-sm text-slate-400">No hay deudas registradas aun.</p>
        ) : (
          <div className="space-y-2">
            {topDeudores.map((c) => (
              <Link
                key={c.customer_id}
                href={`/clientes/${c.customer_id}`}
                className="flex items-center justify-between rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition hover:shadow-md"
              >
                <div>
                  <p className="font-semibold text-slate-900">{c.name}</p>
                  {c.alias && <p className="text-xs text-slate-400">{c.alias}</p>}
                </div>
                <p className="text-lg font-black text-red-600">{formatCurrencyDOP(Number(c.balance))}</p>
              </Link>
            ))}
          </div>
        )}
      </section>

      <nav className="mt-8 flex gap-3">
        <Link
          href="/clientes"
          className="rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
        >
          Ver todos los clientes
        </Link>
      </nav>
      <MobileNav />
    </div>
  );
}
