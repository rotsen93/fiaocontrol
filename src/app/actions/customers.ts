"use server";

import { revalidatePath } from "next/cache";
import { z } from "zod";
import { createServerSupabaseClient } from "@/lib/supabase/server";

const DEMO_BUSINESS_ID = process.env.DEMO_BUSINESS_ID ?? "00000000-0000-0000-0000-000000000001";

const createCustomerSchema = z.object({
  name: z.string().min(1).max(120),
  phone: z.string().min(7).max(20),
  alias: z.string().max(60).optional(),
});

export async function getCustomersWithBalance() {
  const supabase = await createServerSupabaseClient();
  const { data, error } = await supabase
    .from("customer_balances")
    .select("*")
    .eq("business_id", DEMO_BUSINESS_ID)
    .order("balance", { ascending: false });

  if (error) throw new Error(error.message);
  return data ?? [];
}

export async function getCustomerById(id: string) {
  const supabase = await createServerSupabaseClient();
  const { data, error } = await supabase
    .from("customer_balances")
    .select("*")
    .eq("customer_id", id)
    .eq("business_id", DEMO_BUSINESS_ID)
    .single();

  if (error) throw new Error(error.message);
  return data;
}

export async function createCustomer(formData: FormData) {
  const raw = {
    name: formData.get("name") as string,
    phone: formData.get("phone") as string,
    alias: (formData.get("alias") as string) || undefined,
  };
  const parsed = createCustomerSchema.safeParse(raw);
  if (!parsed.success) {
    return { error: "Datos invalidos: " + parsed.error.issues.map((i) => i.message).join(", ") };
  }

  const supabase = await createServerSupabaseClient();
  const { error } = await supabase.from("customers").insert({
    business_id: DEMO_BUSINESS_ID,
    name: parsed.data.name.trim(),
    phone: parsed.data.phone.trim(),
    alias: parsed.data.alias?.trim() ?? null,
  });

  if (error) return { error: error.message };
  revalidatePath("/clientes");
  return { success: true };
}

export async function deleteCustomer(id: string) {
  const supabase = await createServerSupabaseClient();
  const { error } = await supabase
    .from("customers")
    .delete()
    .eq("id", id)
    .eq("business_id", DEMO_BUSINESS_ID);

  if (error) return { error: error.message };
  revalidatePath("/clientes");
  return { success: true };
}
