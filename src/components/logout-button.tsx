"use client";

import { useTransition } from "react";
import { signOut } from "@/app/actions/auth";

export function LogoutButton() {
  const [isPending, startTransition] = useTransition();

  return (
    <button
      onClick={() => startTransition(() => signOut())}
      disabled={isPending}
      className="rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-500 transition hover:bg-slate-50 disabled:opacity-50"
    >
      {isPending ? "Saliendo…" : "Salir"}
    </button>
  );
}
