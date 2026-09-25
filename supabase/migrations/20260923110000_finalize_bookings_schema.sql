-- Run this migration in the Supabase SQL Editor for the project used by the app.
-- The mobile API writes to public.bookings; bookings_api is read-only.
do $$
begin
    if to_regclass('public.bookings') is null then
        raise exception 'public.bookings does not exist; create the base bookings table first';
    end if;

    execute 'alter table public.bookings add column if not exists car_id uuid';
    execute 'alter table public.bookings add column if not exists user_id uuid';
    execute 'alter table public.bookings add column if not exists user_name text';
    execute 'alter table public.bookings add column if not exists booking_date text';
    execute 'alter table public.bookings add column if not exists scheduled_start_time text';
    execute 'alter table public.bookings add column if not exists scheduled_end_time text';
    execute 'alter table public.bookings add column if not exists return_date text';
    execute 'alter table public.bookings add column if not exists return_time text';
    execute 'alter table public.bookings add column if not exists location text';
    execute 'alter table public.bookings add column if not exists sector text';
    execute 'alter table public.bookings add column if not exists is_emergency boolean default false';
    execute 'alter table public.bookings add column if not exists status text default ''agendado''';
end $$;

alter table public.bookings enable row level security;

drop policy if exists "Authenticated users can read bookings" on public.bookings;
drop policy if exists "Users can create their own bookings" on public.bookings;
drop policy if exists "Users and approvers can update bookings" on public.bookings;

create policy "Authenticated users can read bookings"
on public.bookings for select to authenticated
using (
    auth.uid() = user_id
    or exists (
        select 1 from public.profiles
        where profiles.id = auth.uid()
        and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
);

create policy "Users can create their own bookings"
on public.bookings for insert to authenticated
with check (auth.uid() = user_id);

create policy "Users and approvers can update bookings"
on public.bookings for update to authenticated
using (
    auth.uid() = user_id
    or exists (
        select 1 from public.profiles
        where profiles.id = auth.uid()
        and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
)
with check (
    auth.uid() = user_id
    or exists (
        select 1 from public.profiles
        where profiles.id = auth.uid()
        and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
);

notify pgrst, 'reload schema';
