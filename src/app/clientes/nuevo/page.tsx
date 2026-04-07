"use client";

import { useRouter } from "next/navigation";
import { useActionState } from "react";
import { createCustomer } from "@/app/actions/customers";
import Link from "next/link";

type State = { error?: string; success?: boolean } | null;

export default function NuevoClientePage() {
  const router = useRouter();
  const [state, formAction, pending] = useActionState<State, FormData>(
    async (_prev, formData) => {
      const result = await createCustomer(formData);
      if (result.success) {
        router.push("/clientes");
        return { success: true };
      }
      return result;
    },
    null
  );

  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-8">
      <header className="mb-6">
        <Link href="/clientes" className="text-xs text-slate-400 hover:underline">
          ← Clientes
        </Link>
        <h1 className="mt-1 text-xl font-black text-slate-900">Nuevo cliente</h1>
      </header>

      <form action={formAction} className="mx-auto max-w-md space-y-4">
        <div>
          <label className="mb-1 block text-sm font-semibold text-slate-700" htmlFor="name">
            Nombre *
          </label>
          <input
            id="name"
            name="name"
            type="text"
            required
            maxLength={120}
            placeholder="Juan Perez"
            className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none focus:ring-2 focus:ring-slate-900"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-semibold text-slate-700" htmlFor="phone">
            Teléfono *
          </label>
          <input
            id="phone"
            name="phone"
            type="tel"
            required
            maxLength={20}
            placeholder="809-555-1234"
            className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none focus:ring-2 focus:ring-slate-900"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-semibold text-slate-700" htmlFor="alias">
            Alias <span className="font-normal text-slate-400">(opcional)</span>
          </label>
          <input
            id="alias"
            name="alias"
            type="text"
            maxLength={60}
            placeholder="El mecanico, vecina del 3..."
            className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none focus:ring-2 focus:ring-slate-900"
          />
        </div>

        {state?.error && (
          <p className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{state.error}</p>
        )}

        <button
          type="submit"
          disabled={pending}
          className="w-full rounded-xl bg-slate-900 py-3 text-sm font-bold text-white transition hover:bg-slate-700 disabled:opacity-50"
        >
          {pending ? "Guardando..." : "Guardar cliente"}
        </button>
      </form>
    </div>
  );
}
