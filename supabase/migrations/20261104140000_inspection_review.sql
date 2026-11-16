-- Passo 3: o gestor confirma a avaria ou marca como falso alarme.
-- Essa decisão humana vale mais que a da IA e serve para medir onde a IA erra.

alter table public.inspection_reports
    add column if not exists review_status text
        check (review_status in ('confirmado', 'falso_alarme')),
    add column if not exists reviewed_by uuid references auth.users(id) on delete set null,
    add column if not exists reviewed_at bigint;

-- Só ADMIN, SUPERVISOR ou ENCARREGADO podem decidir. p_status = null desfaz a decisão.
create or replace function public.review_inspection(p_trip_id uuid, p_status text)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_trip record;
begin
    if not exists (
        select 1 from public.profiles
        where id = auth.uid()
          and role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    ) then
        raise exception 'Apenas gestores podem revisar vistorias';
    end if;

    if p_status is not null and p_status not in ('confirmado', 'falso_alarme') then
        raise exception 'Status inválido: %', p_status;
    end if;

    select id, car_id, user_id into v_trip from public.trips where id = p_trip_id;
    if v_trip.id is null then
        raise exception 'Viagem não encontrada';
    end if;

    -- Cria o relatório se a viagem ainda não tinha (ex.: viagens antigas sem análise)
    insert into public.inspection_reports as r
        (trip_id, car_id, user_id, houve_novo_dano, resumo_geral, danos_encontrados,
         analysis_failed, review_status, reviewed_by, reviewed_at)
    values
        (v_trip.id, v_trip.car_id, v_trip.user_id, false,
         'Vistoria revisada manualmente pelo gestor.', '[]'::jsonb,
         true, p_status, auth.uid(), (extract(epoch from now()) * 1000)::bigint)
    on conflict (trip_id) do update
        set review_status = excluded.review_status,
            reviewed_by   = excluded.reviewed_by,
            reviewed_at   = excluded.reviewed_at;
end;
$$;

revoke all on function public.review_inspection(uuid, text) from public;
grant execute on function public.review_inspection(uuid, text) to authenticated;

notify pgrst, 'reload schema';
