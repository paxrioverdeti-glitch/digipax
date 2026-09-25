-- Make previously created trips visible with the same destination used by bookings.
alter table public.trips
    add column if not exists destination text,
    add column if not exists end_photo_urls text[] default '{}';

update public.trips t
set destination = b.location
from public.bookings b
where b.id = t.booking_id
  and (t.destination is null or trim(t.destination) = '')
  and b.location is not null;

notify pgrst, 'reload schema';
