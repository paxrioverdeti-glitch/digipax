-- Final RLS policies for trips. The app writes user_id (snake_case).
alter table public.trips enable row level security;

drop policy if exists "Users can create their own trips" on public.trips;
drop policy if exists "Authenticated users can read trips" on public.trips;
drop policy if exists "Users can insert their own trips" on public.trips;
drop policy if exists "Users can read their own trips" on public.trips;
drop policy if exists "Users can update their own trips" on public.trips;

create policy "Users can create their own trips"
on public.trips
for insert
to authenticated
with check (
    user_id is not null
    and user_id = auth.uid()
);

create policy "Authenticated users can read trips"
on public.trips
for select
to authenticated
using (
    user_id = auth.uid()
    or exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
);

create policy "Users can update their own trips"
on public.trips
for update
to authenticated
using (
    user_id = auth.uid()
    or exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
)
with check (
    user_id = auth.uid()
    or exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
);

notify pgrst, 'reload schema';
