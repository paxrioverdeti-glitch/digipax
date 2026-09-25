-- Synchronize legacy records so the user and admin screens show the same data.
do $$
begin
    if exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'bookings' and column_name = 'destination'
    ) then
        execute $sql$
            update public.bookings
            set location = coalesce(nullif(location, ''), destination)
            where (location is null or trim(location) = '')
              and destination is not null
        $sql$;
    end if;
end $$;

update public.trips t
set destination = coalesce(nullif(t.destination, ''), b.location)
from public.bookings b
where b.id = t.booking_id
  and (t.destination is null or trim(t.destination) = '')
  and b.location is not null;

update public.bookings b
set
    initial_km = coalesce(b.initial_km, t.start_km),
    final_km = coalesce(b.final_km, t.end_km),
    start_time = coalesce(b.start_time, to_char(to_timestamp(t.start_time / 1000.0), 'HH24:MI')),
    end_time = coalesce(b.end_time, to_char(to_timestamp(t.end_time / 1000.0), 'HH24:MI'))
from public.trips t
where t.booking_id = b.id;

notify pgrst, 'reload schema';
