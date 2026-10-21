-- Keep the physical booking destination canonical and repair existing trips.
alter table public.bookings
    add column if not exists location text;

do $$
begin
    if exists (
        select 1 from information_schema.columns
        where table_schema = 'public'
          and table_name = 'bookings'
          and column_name = 'destination'
    ) then
        execute $sql$
            update public.bookings
            set location = coalesce(nullif(trim(location), ''), nullif(trim(destination), ''))
            where location is null or trim(location) = ''
        $sql$;
    end if;
end $$;

update public.trips t
set destination = b.location
from public.bookings b
where b.id = t.booking_id
  and (t.destination is null or trim(t.destination) = '')
  and b.location is not null
  and trim(b.location) <> '';

notify pgrst, 'reload schema';
