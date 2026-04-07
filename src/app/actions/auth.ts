"use server";

import { redirect } from "next/navigation";
import { createServerSupabaseClient } from "@/lib/supabase/server";

export type AuthState = { error?: string; success?: string } | undefined;

export async function signIn(
  _state: AuthState,
  formData: FormData
): Promise<AuthState> {
  const email = (formData.get("email") as string)?.trim();
  const password = formData.get("password") as string;

  if (!email || !password) {
    return { error: "Correo y contraseña son requeridos." };
  }

  const supabase = await createServerSupabaseClient();
  const { error } = await supabase.auth.signInWithPassword({ email, password });

  if (error) {
    const msg = error.message.toLowerCase();
    if (msg.includes("invalid login credentials")) {
      return { error: "Correo o contraseña incorrectos." };
    }
    if (msg.includes("email not confirmed")) {
      return { error: "Primero confirma tu correo. Revisa tu bandeja de entrada." };
    }
    if (msg.includes("rate limit") || msg.includes("too many")) {
      return { error: "Demasiados intentos. Espera unos minutos e intenta de nuevo." };
    }
    return { error: error.message };
  }

  redirect("/dashboard");
}

export async function signUp(
  _state: AuthState,
  formData: FormData
): Promise<AuthState> {
  const email = (formData.get("email") as string)?.trim();
  const password = formData.get("password") as string;

  if (!email || !password) {
    return { error: "Correo y contraseña son requeridos." };
  }
  if (password.length < 8) {
    return { error: "La contraseña debe tener al menos 8 caracteres." };
  }

  const supabase = await createServerSupabaseClient();
  const { data, error } = await supabase.auth.signUp({ email, password });

  if (error) {
    const msg = error.message.toLowerCase();
    if (msg.includes("rate limit") || msg.includes("over_email_send_rate_limit")) {
      return { error: "Límite de correos alcanzado. Espera unos minutos." };
    }
    if (msg.includes("already registered") || msg.includes("user already exists")) {
      return { error: "Este correo ya está registrado. Inicia sesión." };
    }
    if (msg.includes("invalid") && msg.includes("email")) {
      return { error: "Correo electrónico no válido." };
    }
    return { error: error.message };
  }

  // Usuario creado pero requiere confirmación de correo
  if (data.user && !data.session) {
    return {
      success:
        "¡Cuenta creada! Revisa tu correo y confirma tu cuenta para iniciar sesión.",
    };
  }

  // Auto-confirmado (usuario creado desde Supabase Dashboard)
  redirect("/dashboard");
}

export async function signOut() {
  const supabase = await createServerSupabaseClient();
  await supabase.auth.signOut();
  redirect("/login");
}
