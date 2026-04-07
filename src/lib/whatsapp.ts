import { formatCurrencyDOP, formatDateTimeDO } from "@/lib/format";
import type { WhatsappMessageInput } from "@/lib/types";

function normalizePhone(phone: string): string {
  const digits = phone.replace(/[^\d]/g, "");
  // Numeros de 10 digitos (RD: 809/829/849-XXX-XXXX) → agregar codigo de pais 1
  if (digits.length === 10) return "1" + digits;
  // Ya tiene codigo de pais incluido
  return digits;
}

export function buildWhatsappMessage(input: WhatsappMessageInput): string {
  const actionText = input.type === "fiao" ? "se registro un fiao" : "se registro un abono";

  return [
    `Hola ${input.customerName}, ${actionText} de ${formatCurrencyDOP(input.amount)} en ${input.businessName}.`,
    `Balance actual: ${formatCurrencyDOP(input.balance)}.`,
    `Fecha: ${formatDateTimeDO(input.date)}.`,
  ].join(" ");
}

export function buildWhatsappUrl(phone: string, message: string): string {
  const normalized = normalizePhone(phone);
  return `https://wa.me/${normalized}?text=${encodeURIComponent(message)}`;
}
