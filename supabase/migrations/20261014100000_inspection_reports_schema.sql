-- Tabela para armazenar os relatórios de vistoria veicular gerados pela IA (Gemini)
create table if not exists public.inspection_reports (
    id uuid primary key default gen_random_uuid(),
    trip_id uuid references public.trips(id) on delete cascade,
    car_id uuid references public.cars(id) on delete set null,
    user_id uuid references auth.users(id) on delete set null,
    houve_novo_dano boolean not null default false,
    resumo_geral text not null default '',
    danos_encontrados jsonb not null default '[]'::jsonb,
    analyzed_at bigint not null default (extract(epoch from now()) * 1000)::bigint,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- Habilita RLS (Row Level Security)
alter table public.inspection_reports enable row level security;

-- Remove políticas anteriores se existirem
drop policy if exists "Authenticated users can insert inspection reports" on public.inspection_reports;
drop policy if exists "Only admin and managers can view inspection reports" on public.inspection_reports;

-- Política 1: Qualquer usuário autenticado (motorista) pode inserir o relatório de vistoria
create policy "Authenticated users can insert inspection reports"
on public.inspection_reports
for insert
to authenticated
with check (true);

-- Política 2: Apenas Gestores/Administradores podem visualizar os relatórios de vistoria
create policy "Only admin and managers can view inspection reports"
on public.inspection_reports
for select
to authenticated
using (
    exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
);

-- Habilita o Supabase Realtime para a tabela de relatórios de vistoria
do $$
begin
    if not exists (
        select 1
        from pg_publication_tables
        where pubname = 'supabase_realtime'
          and schemaname = 'public'
          and tablename = 'inspection_reports'
    ) then
        alter publication supabase_realtime add table public.inspection_reports;
    end if;
end $$;

-- Índices para otimização de consultas por viagem e veículo
create index if not exists idx_inspection_reports_trip_id on public.inspection_reports(trip_id);
create index if not exists idx_inspection_reports_car_id on public.inspection_reports(car_id);

notify pgrst, 'reload schema';
