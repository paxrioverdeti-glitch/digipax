-- `bookings_api` uses camelCase aliases, but writes go to this physical table.
-- Keep all fields used by the application available with stable snake_case names.
alter table public.bookings
    add column if not exists car_id uuid,
    add column if not exists user_id uuid,
    add column if not exists user_name text,
    add column if not exists booking_date text,
    add column if not exists scheduled_start_time text,
    add column if not exists scheduled_end_time text,
    add column if not exists return_date text,
    add column if not exists return_time text,
    add column if not exists location text,
    add column if not exists sector text,
    add column if not exists is_emergency boolean not null default false,
    add column if not exists status text not null default 'agendado';

alter table public.bookings enable row level security;

drop policy if exists "Authenticated users can read bookings" on public.bookings;
drop policy if exists "Users can create their own bookings" on public.bookings;
drop policy if exists "Users and approvers can update bookings" on public.bookings;

create policy "Authenticated users can read bookings"
on public.bookings
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

create policy "Users can create their own bookings"
on public.bookings
for insert
to authenticated
with check (auth.uid() = user_id);

create policy "Users and approvers can update bookings"
on public.bookings
for update
to authenticated
using (
    auth.uid() = user_id
    or exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
)
with check (
    auth.uid() = user_id
    or exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
);

notify pgrst, 'reload schema';
