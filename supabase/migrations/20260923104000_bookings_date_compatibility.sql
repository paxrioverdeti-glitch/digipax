-- The physical table uses `booking_date`; the read view exposes it as `bookingDate`.
alter table public.bookings
    add column if not exists booking_date text;
