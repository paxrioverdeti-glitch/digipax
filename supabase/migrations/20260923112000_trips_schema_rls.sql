-- Trip writes use the physical snake_case columns.
alter table public.trips
    add column if not exists user_id uuid,
    add column if not exists booking_id uuid,
    add column if not exists car_id uuid,
    add column if not exists start_km numeric,
    add column if not exists start_time bigint,
    add column if not exists photo_urls text[] default '{}',
    add column if not exists status text default 'ativo';

alter table public.trips enable row level security;

drop policy if exists "Users can create their own trips" on public.trips;
create policy "Users can create their own trips"
on public.trips
for insert
to authenticated
with check (auth.uid() = user_id);

drop policy if exists "Authenticated users can read trips" on public.trips;
create policy "Authenticated users can read trips"
on public.trips
for select
to authenticated
using (
    auth.uid() = user_id
    or exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
);

notify pgrst, 'reload schema';
