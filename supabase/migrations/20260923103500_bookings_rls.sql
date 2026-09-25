alter table public.bookings enable row level security;

drop policy if exists "Authenticated users can read bookings"
on public.bookings;

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

drop policy if exists "Users can create their own bookings"
on public.bookings;

create policy "Users can create their own bookings"
on public.bookings
for insert
to authenticated
with check (auth.uid() = user_id);

drop policy if exists "Users and approvers can update bookings"
on public.bookings;

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
