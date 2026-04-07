"use client";

import { useActionState, useState } from "react";
import { signIn, signUp, type AuthState } from "@/app/actions/auth";

function LoginForm() {
  const [state, action, isPending] = useActionState<AuthState, FormData>(
    signIn,
    undefined
  );

  return (
    <form action={action} className="space-y-4">
      <div>
        <label htmlFor="login-email" className="block text-sm font-semibold text-slate-700 mb-1">
          Correo electrónico
        </label>
        <input
          id="login-email"
          name="email"
          type="email"
          autoComplete="email"
          required
          className="w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-900"
          placeholder="tu@correo.com"
        />
      </div>
      <div>
        <label htmlFor="login-password" className="block text-sm font-semibold text-slate-700 mb-1">
          Contraseña
        </label>
        <input
          id="login-password"
          name="password"
          type="password"
          autoComplete="current-password"
          required
          className="w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-900"
          placeholder="••••••••"
        />
      </div>
      {state?.error && (
        <p className="rounded-xl bg-red-50 border border-red-200 px-4 py-2.5 text-sm text-red-700">
          {state.error}
        </p>
      )}
      <button
        type="submit"
        disabled={isPending}
        className="w-full rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {isPending ? "Iniciando sesión…" : "Iniciar sesión"}
      </button>
    </form>
  );
}

function SignupForm() {
  const [state, action, isPending] = useActionState<AuthState, FormData>(
    signUp,
    undefined
  );

  return (
    <form action={action} className="space-y-4">
      <div>
        <label htmlFor="signup-email" className="block text-sm font-semibold text-slate-700 mb-1">
          Correo electrónico
        </label>
        <input
          id="signup-email"
          name="email"
          type="email"
          autoComplete="email"
          required
          className="w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-900"
          placeholder="tu@correo.com"
        />
      </div>
      <div>
        <label htmlFor="signup-password" className="block text-sm font-semibold text-slate-700 mb-1">
          Contraseña
        </label>
        <input
          id="signup-password"
          name="password"
          type="password"
          autoComplete="new-password"
          required
          minLength={8}
          className="w-full rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-900"
          placeholder="Mínimo 8 caracteres"
        />
      </div>
      {state?.error && (
        <p className="rounded-xl bg-red-50 border border-red-200 px-4 py-2.5 text-sm text-red-700">
          {state.error}
        </p>
      )}
      {state?.success && (
        <p className="rounded-xl bg-emerald-50 border border-emerald-200 px-4 py-2.5 text-sm text-emerald-700">
          {state.success}
        </p>
      )}
      <button
        type="submit"
        disabled={isPending}
        className="w-full rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {isPending ? "Creando cuenta…" : "Crear cuenta"}
      </button>
    </form>
  );
}

export default function LoginPage() {
  const [mode, setMode] = useState<"login" | "signup">("login");

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-sm">
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-black text-slate-900">FiaoControl</h1>
          <p className="mt-1 text-sm text-slate-500">Cuaderno digital de fiaos</p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="mb-6 flex rounded-xl bg-slate-100 p-1">
            <button
              onClick={() => setMode("login")}
              className={`flex-1 rounded-lg py-2 text-sm font-semibold transition ${
                mode === "login"
                  ? "bg-white text-slate-900 shadow-sm"
                  : "text-slate-500 hover:text-slate-700"
              }`}
            >
              Iniciar sesión
            </button>
            <button
              onClick={() => setMode("signup")}
              className={`flex-1 rounded-lg py-2 text-sm font-semibold transition ${
                mode === "signup"
                  ? "bg-white text-slate-900 shadow-sm"
                  : "text-slate-500 hover:text-slate-700"
              }`}
            >
              Crear cuenta
            </button>
          </div>

          {mode === "login" ? <LoginForm /> : <SignupForm />}
        </div>
      </div>
    </div>
  );
}
