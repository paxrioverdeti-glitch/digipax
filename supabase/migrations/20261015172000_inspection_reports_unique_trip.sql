create unique index if not exists idx_inspection_reports_trip_id_unique
    on public.inspection_reports (trip_id);

notify pgrst, 'reload schema';
