"use client";

import { useActionState } from "react";
import { createLedgerEntry } from "@/app/actions/ledger";

type CustomerOption = {
  customer_id: string;
  name: string;
  alias: string | null;
};

type State = { error?: string; success?: boolean } | null;

const QUICK_AMOUNTS = [100, 200, 300, 500];

export function QuickEntryForm({ customers }: { customers: CustomerOption[] }) {
  const [state, formAction, pending] = useActionState<State, FormData>(
    async (_prev, formData) => createLedgerEntry(formData),
    null
  );

  return (
    <form action={formAction} className="mb-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="text-sm font-black tracking-wide text-slate-800 uppercase">Modo mostrador</h2>
      <p className="mt-1 text-xs text-slate-500">Registro rapido sin entrar al detalle del cliente.</p>

      <div className="mt-4 grid gap-3 sm:grid-cols-2">
        <div>
          <label htmlFor="customer_id" className="mb-1 block text-xs font-semibold text-slate-700">
            Cliente
          </label>
          <select
            id="customer_id"
            name="customer_id"
            required
            className="w-full rounded-xl border border-slate-200 bg-white px-3 py-3 text-sm text-slate-900 outline-none focus:ring-2 focus:ring-slate-900"
          >
            <option value="">Selecciona un cliente</option>
            {customers.map((customer) => (
              <option key={customer.customer_id} value={customer.customer_id}>
                {customer.name}
                {customer.alias ? ` (${customer.alias})` : ""}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="amount" className="mb-1 block text-xs font-semibold text-slate-700">
            Monto (RD$)
          </label>
          <input
            id="amount"
            name="amount"
            type="number"
            min="1"
            step="1"
            required
            inputMode="numeric"
            placeholder="450"
            className="w-full rounded-xl border border-slate-200 bg-white px-3 py-3 text-base font-bold text-slate-900 outline-none focus:ring-2 focus:ring-slate-900"
          />
        </div>
      </div>

      <div className="mt-3 flex flex-wrap gap-2">
        {QUICK_AMOUNTS.map((amount) => (
          <button
            key={amount}
            type="button"
            onClick={(event) => {
              const form = event.currentTarget.form;
              if (!form) return;
              const amountInput = form.elements.namedItem("amount") as HTMLInputElement | null;
              if (amountInput) amountInput.value = String(amount);
            }}
            className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs font-bold text-slate-700 transition hover:bg-slate-100"
          >
            RD${amount}
          </button>
        ))}
      </div>

      <div className="mt-4 grid grid-cols-2 gap-3">
        <button
          type="submit"
          name="type"
          value="fiao"
          disabled={pending}
          className="rounded-xl bg-red-600 py-3 text-sm font-black text-white transition hover:bg-red-700 disabled:opacity-60"
        >
          {pending ? "Guardando..." : "+ Fiao"}
        </button>
        <button
          type="submit"
          name="type"
          value="abono"
          disabled={pending}
          className="rounded-xl bg-emerald-600 py-3 text-sm font-black text-white transition hover:bg-emerald-700 disabled:opacity-60"
        >
          {pending ? "Guardando..." : "- Abono"}
        </button>
      </div>

      <input type="hidden" name="note" value="" />

      {state?.error && (
        <p className="mt-3 rounded-xl bg-red-50 px-3 py-2 text-sm text-red-700">{state.error}</p>
      )}
      {state?.success && (
        <p className="mt-3 rounded-xl bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
          Movimiento guardado.
        </p>
      )}
    </form>
  );
}