alter table public.trips
    add column if not exists destination text;

notify pgrst, 'reload schema';
