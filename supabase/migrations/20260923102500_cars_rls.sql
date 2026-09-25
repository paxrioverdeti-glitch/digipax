alter table public.cars enable row level security;

drop policy if exists "Authenticated users can read cars"
on public.cars;

create policy "Authenticated users can read cars"
on public.cars
for select
to authenticated
using (true);

drop policy if exists "Admins can insert cars"
on public.cars;

create policy "Admins can insert cars"
on public.cars
for insert
to authenticated
with check (
    exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text = 'ADMIN'
    )
);

drop policy if exists "Admins can update cars"
on public.cars;

create policy "Admins can update cars"
on public.cars
for update
to authenticated
using (
    exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text = 'ADMIN'
    )
)
with check (
    exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text = 'ADMIN'
    )
);

drop policy if exists "Admins can delete cars"
on public.cars;

create policy "Admins can delete cars"
on public.cars
for delete
to authenticated
using (
    exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text = 'ADMIN'
    )
);
