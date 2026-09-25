-- Keep the real check-in/check-out values available in the bookings API.
alter table public.bookings
    add column if not exists start_time text,
    add column if not exists end_time text,
    add column if not exists initial_km numeric,
    add column if not exists final_km numeric;

notify pgrst, 'reload schema';
