import { notFound } from "next/navigation";
import Link from "next/link";
import { getCustomerById } from "@/app/actions/customers";
import { getLedgerByCustomer } from "@/app/actions/ledger";
import { formatCurrencyDOP, formatDateTimeDO } from "@/lib/format";
import { buildWhatsappMessage, buildWhatsappUrl } from "@/lib/whatsapp";
import { EntryForm } from "./EntryForm";
import { MobileNav } from "@/components/mobile-nav";

interface Props {
  params: Promise<{ id: string }>;
}

export default async function ClienteDetallePage({ params }: Props) {
  const { id } = await params;

  const [customer, entries] = await Promise.all([
    getCustomerById(id).catch(() => null),
    getLedgerByCustomer(id),
  ]);

  if (!customer) notFound();

  const balance = Number(customer.balance);

  const lastEntry = entries[0];
  const lastAmount = lastEntry ? Number(lastEntry.amount) : 0;

  const waMessage = buildWhatsappMessage({
    type: lastEntry?.type ?? "fiao",
    customerName: customer.name,
    businessName: process.env.NEXT_PUBLIC_APP_NAME ?? "FiaoControl",
    amount: lastAmount,
    balance,
    date: lastEntry ? new Date(lastEntry.created_at) : new Date(),
  });

  const waUrl = buildWhatsappUrl(customer.phone, waMessage);

  return (
    <div className="min-h-screen bg-slate-50 p-4 pb-24 sm:p-8 sm:pb-8">
      <header className="mb-6 flex items-start justify-between">
        <div>
          <Link href="/clientes" className="text-xs text-slate-400 hover:underline">
            ← Clientes
          </Link>
          <h1 className="mt-1 text-xl font-black text-slate-900">{customer.name}</h1>
          <p className="text-xs text-slate-400">
            {customer.phone}
            {customer.alias ? ` · ${customer.alias}` : ""}
          </p>
        </div>
        <div className="text-right">
          <p className="text-xs font-semibold tracking-widest text-slate-500 uppercase">Balance</p>
          <p className={`text-2xl font-black ${balance > 0 ? "text-red-600" : "text-emerald-600"}`}>
            {formatCurrencyDOP(balance)}
          </p>
        </div>
      </header>

      {lastEntry && (
        <a
          href={waUrl}
          target="_blank"
          rel="noreferrer"
          className="mb-6 flex w-full items-center justify-center gap-2 rounded-xl bg-emerald-700 py-3 text-sm font-bold text-white transition hover:bg-emerald-800"
        >
          📲 Enviar último movimiento por WhatsApp
        </a>
      )}

      <EntryForm customerId={id} />

      <section className="mt-8">
        <h2 className="mb-3 text-xs font-bold tracking-widest text-slate-500 uppercase">Historial</h2>
        {entries.length === 0 ? (
          <p className="text-sm text-slate-400">Sin movimientos aun.</p>
        ) : (
          <div className="space-y-2">
            {entries.map((e) => (
              <div
                key={e.id}
                className="flex items-center justify-between rounded-2xl border border-slate-200 bg-white p-4 shadow-sm"
              >
                <div>
                  <span
                    className={`inline-flex rounded-full px-2 py-0.5 text-xs font-bold uppercase ${
                      e.type === "fiao"
                        ? "bg-red-100 text-red-700"
                        : "bg-emerald-100 text-emerald-700"
                    }`}
                  >
                    {e.type}
                  </span>
                  {e.note && <p className="mt-1 text-xs text-slate-500">{e.note}</p>}
                  <p className="mt-0.5 text-xs text-slate-400">
                    {formatDateTimeDO(new Date(e.created_at))}
                  </p>
                </div>
                <p
                  className={`text-lg font-black ${
                    e.type === "fiao" ? "text-red-600" : "text-emerald-600"
                  }`}
                >
                  {e.type === "fiao" ? "+" : "-"}
                  {formatCurrencyDOP(Number(e.amount))}
                </p>
              </div>
            ))}
          </div>
        )}
      </section>
      <MobileNav />
    </div>
  );
}
