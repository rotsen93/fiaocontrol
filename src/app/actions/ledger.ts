"use server";

import { revalidatePath } from "next/cache";
import { z } from "zod";
import { createServerSupabaseClient } from "@/lib/supabase/server";
import type { LedgerEntryType } from "@/lib/types";

const DEMO_BUSINESS_ID = process.env.DEMO_BUSINESS_ID ?? "00000000-0000-0000-0000-000000000001";

const createEntrySchema = z.object({
  customer_id: z.string().uuid(),
  type: z.enum(["fiao", "abono"]),
  amount: z.coerce.number().positive("El monto debe ser mayor a 0"),
  note: z.string().max(200).optional(),
});

export async function getLedgerByCustomer(customerId: string) {
  const supabase = await createServerSupabaseClient();
  const { data, error } = await supabase
    .from("ledger_entries")
    .select("*")
    .eq("customer_id", customerId)
    .eq("business_id", DEMO_BUSINESS_ID)
    .order("created_at", { ascending: false });

  if (error) throw new Error(error.message);
  return data ?? [];
}

export async function createLedgerEntry(formData: FormData) {
  const raw = {
    customer_id: formData.get("customer_id") as string,
    type: formData.get("type") as LedgerEntryType,
    amount: formData.get("amount"),
    note: (formData.get("note") as string) || undefined,
  };
  const parsed = createEntrySchema.safeParse(raw);
  if (!parsed.success) {
    return { error: "Datos invalidos: " + parsed.error.issues.map((i) => i.message).join(", ") };
  }

  const supabase = await createServerSupabaseClient();
  const { error } = await supabase.from("ledger_entries").insert({
    business_id: DEMO_BUSINESS_ID,
    customer_id: parsed.data.customer_id,
    type: parsed.data.type,
    amount: parsed.data.amount,
    note: parsed.data.note?.trim() ?? null,
  });

  if (error) return { error: error.message };
  revalidatePath(`/clientes/${parsed.data.customer_id}`);
  revalidatePath("/clientes");
  revalidatePath("/dashboard");
  return { success: true };
}

export async function getDashboardTotals() {
  const supabase = await createServerSupabaseClient();
  const { data, error } = await supabase
    .from("customer_balances")
    .select("balance")
    .eq("business_id", DEMO_BUSINESS_ID);

  if (error) throw new Error(error.message);
  const total = (data ?? []).reduce((sum, row) => sum + Number(row.balance), 0);
  return { totalDebt: total, customerCount: (data ?? []).length };
}
