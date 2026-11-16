-- Salvar relatório de vistoria por função segura (RPC), sem depender das regras
-- de linha (RLS) da tabela. O usuário é sempre o dono logado (auth.uid()).
-- Também marca análises que falharam, para o admin revisar as fotos manualmente.

alter table public.inspection_reports
    add column if not exists analysis_failed boolean not null default false;

create or replace function public.save_inspection_report(
    p_trip_id uuid,
    p_car_id uuid,
    p_houve_novo_dano boolean,
    p_resumo_geral text,
    p_danos jsonb,
    p_analysis_failed boolean default false
)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
    if auth.uid() is null then
        raise exception 'Usuário não autenticado';
    end if;

    -- Só o motorista dono da viagem pode registrar a vistoria dela
    if not exists (
        select 1 from public.trips t
        where t.id = p_trip_id and t.user_id = auth.uid()
    ) then
        raise exception 'Viagem não pertence ao usuário';
    end if;

    insert into public.inspection_reports as r
        (trip_id, car_id, user_id, houve_novo_dano, resumo_geral, danos_encontrados, analysis_failed)
    values
        (p_trip_id, p_car_id, auth.uid(), p_houve_novo_dano, coalesce(p_resumo_geral, ''),
         coalesce(p_danos, '[]'::jsonb), coalesce(p_analysis_failed, false))
    on conflict (trip_id) do update
        set houve_novo_dano   = excluded.houve_novo_dano,
            resumo_geral      = excluded.resumo_geral,
            danos_encontrados = excluded.danos_encontrados,
            analysis_failed   = excluded.analysis_failed,
            analyzed_at       = (extract(epoch from now()) * 1000)::bigint
        where r.analysis_failed; -- só substitui uma análise que tinha falhado
end;
$$;

revoke all on function public.save_inspection_report(uuid, uuid, boolean, text, jsonb, boolean) from public;
grant execute on function public.save_inspection_report(uuid, uuid, boolean, text, jsonb, boolean) to authenticated;

notify pgrst, 'reload schema';
