-- Cancelar comunicado de ausência (pelo próprio colaborador).
-- O comunicado NÃO é apagado: fica marcado como cancelado, para o RH manter o histórico.
-- Só pode cancelar quem criou, e só enquanto o gestor ainda não avaliou.
--
-- Seguro para versões antigas do app: só acrescenta uma coluna no fim da view.

alter table public.absences
    add column if not exists cancelled_at bigint;

-- Função segura: o colaborador não ganha permissão geral de editar a tabela,
-- só consegue marcar o PRÓPRIO comunicado, ainda não avaliado, como cancelado.
create or replace function public.cancel_absence(p_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_absence record;
begin
    select id, user_id, admin_feedback, cancelled_at
    into v_absence
    from public.absences
    where id = p_id
    for update; -- trava a linha: evita cancelar e avaliar ao mesmo tempo

    if v_absence.id is null then
        raise exception 'Comunicado não encontrado';
    end if;

    if v_absence.user_id::text <> (select auth.uid())::text then
        raise exception 'Você só pode cancelar os seus próprios comunicados';
    end if;

    if v_absence.cancelled_at is not null then
        return; -- já estava cancelado
    end if;

    if v_absence.admin_feedback is not null
       and v_absence.admin_feedback <> 'null'::jsonb then
        raise exception 'Este comunicado já foi avaliado e não pode mais ser cancelado';
    end if;

    update public.absences
    set cancelled_at = (extract(epoch from now()) * 1000)::bigint
    where id = p_id;
end;
$$;

revoke all on function public.cancel_absence(uuid) from public;
revoke all on function public.cancel_absence(uuid) from anon;
grant execute on function public.cancel_absence(uuid) to authenticated;

-- A coluna nova entra no FINAL da view (exigência do create or replace).
create or replace view public.absences_api as
select
    a.id,
    a.user_id as "userId",
    a.user_name as "userName",
    a.avatar_url,
    a.sector,
    a.target_approver_id as "targetApproverId",
    a.target_approver_name as "targetApproverName",
    a.type,
    a.date,
    a.expected_time as "expectedTime",
    a.effective_time as "effectiveTime",
    a.exit_time as "exitTime",
    a.return_time as "returnTime",
    a.missing_hours as "missingHours",
    a.missing_days as "missingDays",
    a.original_time as "originalTime",
    a.new_time as "newTime",
    a.reason,
    a.admin_feedback as "adminFeedback",
    a.created_at as "createdAt",
    a.cancelled_at as "cancelledAt"
from public.absences a;

alter view public.absences_api set (security_invoker = true);

notify pgrst, 'reload schema';
