"use client";

import { useActionState, useRef } from "react";
import { createLedgerEntry } from "@/app/actions/ledger";

type State = { error?: string; success?: boolean } | null;
const QUICK_AMOUNTS = [100, 200, 300, 500, 1000];

export function EntryForm({ customerId }: { customerId: string }) {
  const amountRef = useRef<HTMLInputElement>(null);
  const [state, formAction, pending] = useActionState<State, FormData>(
    async (_prev, formData) => {
      const result = await createLedgerEntry(formData);
      return result;
    },
    null
  );

  return (
    <form action={formAction} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <input type="hidden" name="customer_id" value={customerId} />

      <h2 className="mb-4 text-sm font-bold text-slate-700">Registrar movimiento</h2>

      <div className="mb-4 grid grid-cols-2 gap-3">
        <label className="flex cursor-pointer items-center justify-center gap-2 rounded-xl border-2 border-red-200 bg-red-50 p-3 text-sm font-bold text-red-700 has-[:checked]:border-red-600 has-[:checked]:bg-red-100">
          <input type="radio" name="type" value="fiao" defaultChecked className="sr-only" />
          Fiao
        </label>
        <label className="flex cursor-pointer items-center justify-center gap-2 rounded-xl border-2 border-emerald-200 bg-emerald-50 p-3 text-sm font-bold text-emerald-700 has-[:checked]:border-emerald-600 has-[:checked]:bg-emerald-100">
          <input type="radio" name="type" value="abono" className="sr-only" />
          Abono
        </label>
      </div>

      <div className="mb-3">
        <label className="mb-1 block text-xs font-semibold text-slate-700" htmlFor="amount">
          Monto (RD$) *
        </label>
        <input
          id="amount"
          name="amount"
          ref={amountRef}
          type="number"
          min="1"
          step="1"
          inputMode="numeric"
          required
          autoFocus
          placeholder="450"
          className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-base font-bold text-slate-900 outline-none focus:ring-2 focus:ring-slate-900"
        />
      </div>

      <div className="mb-4 flex flex-wrap gap-2">
        {QUICK_AMOUNTS.map((amount) => (
          <button
            key={amount}
            type="button"
            onClick={() => {
              if (!amountRef.current) return;
              amountRef.current.value = String(amount);
              amountRef.current.focus();
            }}
            className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs font-bold text-slate-700 transition hover:bg-slate-100"
          >
            RD${amount}
          </button>
        ))}
      </div>

      <div className="mb-4">
        <label className="mb-1 block text-xs font-semibold text-slate-700" htmlFor="note">
          Nota <span className="font-normal text-slate-400">(opcional)</span>
        </label>
        <input
          id="note"
          name="note"
          type="text"
          maxLength={200}
          placeholder="2 fundas de arroz..."
          className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none focus:ring-2 focus:ring-slate-900"
        />
      </div>

      {state?.error && (
        <p className="mb-3 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{state.error}</p>
      )}
      {state?.success && (
        <p className="mb-3 rounded-xl bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
          Movimiento registrado.
        </p>
      )}

      <button
        type="submit"
        disabled={pending}
        className="w-full rounded-xl bg-slate-900 py-3 text-sm font-bold text-white transition hover:bg-slate-700 disabled:opacity-50"
      >
        {pending ? "Guardando..." : "Guardar movimiento"}
      </button>
    </form>
  );
}
