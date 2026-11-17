-- Lista de aprovadores (supervisores) para o comunicado de ausência.
--
-- Problema: o app lia a tabela profiles direto. Pelas regras de acesso (RLS),
-- um colaborador comum pode não enxergar o perfil de outras pessoas, então só
-- aparecia a lista fixa escrita no código, e supervisor novo nunca entrava.
--
-- Solução: uma função segura que devolve SÓ id, nome, e-mail e papel de quem é
-- ADMIN, SUPERVISOR ou ENCARREGADO. Nada de outros dados pessoais.
-- Só usuários logados podem chamar.

create or replace function public.list_approvers()
returns table (id uuid, name text, email text, role text)
language sql
stable
security definer
set search_path = public
as $$
    select p.id,
           coalesce(nullif(p.name, ''), split_part(p.email, '@', 1)) as name,
           p.email,
           upper(p.role::text) as role
    from public.profiles p
    where (select auth.uid()) is not null
      and upper(p.role::text) in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
      and p.email is not null
    order by 2;
$$;

revoke all on function public.list_approvers() from public;
revoke all on function public.list_approvers() from anon;
grant execute on function public.list_approvers() to authenticated;

notify pgrst, 'reload schema';
