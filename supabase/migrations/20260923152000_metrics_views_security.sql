-- Do not bypass RLS on dashboard read models.
alter view public.km_by_month set (security_invoker = true);
alter view public.ticket_metrics_by_sector set (security_invoker = true);

notify pgrst, 'reload schema';
