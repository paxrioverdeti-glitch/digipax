-- Read models consumed by the administrative dashboard.
create or replace view public.km_by_month as
select
    to_char(to_timestamp(start_time / 1000.0), 'Mon') as month,
    coalesce(sum(greatest(coalesce(end_km, start_km) - start_km, 0)), 0)::double precision as total_km
from public.trips
where start_time is not null
group by to_char(to_timestamp(start_time / 1000.0), 'Mon');

create or replace view public.ticket_metrics_by_sector as
select
    coalesce(sector, 'Não informado') as sector,
    0::double precision as "averageResolutionTimeHours",
    count(*)::integer as total_tickets,
    count(*) filter (where lower(status::text) = 'resolvido')::integer as resolved_count
from public.tickets
group by coalesce(sector, 'Não informado');

alter view public.km_by_month set (security_invoker = true);
alter view public.ticket_metrics_by_sector set (security_invoker = true);

grant select on public.km_by_month to authenticated;
grant select on public.ticket_metrics_by_sector to authenticated;

notify pgrst, 'reload schema';
