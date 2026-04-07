-- FiaoControl MVP schema

create extension if not exists "pgcrypto";

create table if not exists public.businesses (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  created_at timestamptz not null default now()
);

create table if not exists public.customers (
  id uuid primary key default gen_random_uuid(),
  business_id uuid not null references public.businesses(id) on delete cascade,
  name text not null,
  phone text not null,
  alias text,
  created_at timestamptz not null default now()
);

create type public.ledger_entry_type as enum ('fiao', 'abono');

create table if not exists public.ledger_entries (
  id uuid primary key default gen_random_uuid(),
  business_id uuid not null references public.businesses(id) on delete cascade,
  customer_id uuid not null references public.customers(id) on delete cascade,
  type public.ledger_entry_type not null,
  amount numeric(10,2) not null check (amount > 0),
  note text,
  created_by uuid,
  created_at timestamptz not null default now()
);

create table if not exists public.user_businesses (
  user_id uuid not null,
  business_id uuid not null references public.businesses(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (user_id, business_id)
);

create index if not exists idx_customers_business_id on public.customers(business_id);
create index if not exists idx_ledger_entries_business_id on public.ledger_entries(business_id);
create index if not exists idx_ledger_entries_customer_id on public.ledger_entries(customer_id);
create index if not exists idx_ledger_entries_created_at on public.ledger_entries(created_at desc);
create index if not exists idx_user_businesses_user_id on public.user_businesses(user_id);

create or replace view public.customer_balances as
select
  c.id as customer_id,
  c.business_id,
  c.name,
  c.phone,
  c.alias,
  coalesce(sum(case when le.type = 'fiao' then le.amount else -le.amount end), 0)::numeric(10,2) as balance
from public.customers c
left join public.ledger_entries le on le.customer_id = c.id
group by c.id, c.business_id, c.name, c.phone, c.alias;

-- Seed: negocio demo (necesario para MVP sin autenticacion)
insert into public.businesses (id, name)
values ('00000000-0000-0000-0000-000000000001', 'FiaoControl Demo')
on conflict (id) do nothing;
