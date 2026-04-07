export type LedgerEntryType = "fiao" | "abono";

export interface Business {
  id: string;
  name: string;
  created_at: string;
}

export interface Customer {
  id: string;
  business_id: string;
  name: string;
  phone: string;
  alias: string | null;
  created_at: string;
}

export interface CustomerBalance extends Customer {
  balance: number;
}

export interface LedgerEntry {
  id: string;
  business_id: string;
  customer_id: string;
  type: LedgerEntryType;
  amount: number;
  note: string | null;
  created_by: string | null;
  created_at: string;
}

export interface WhatsappMessageInput {
  type: LedgerEntryType;
  customerName: string;
  businessName: string;
  amount: number;
  balance: number;
  date: Date;
}
