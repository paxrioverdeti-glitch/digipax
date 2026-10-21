-- ===========================================================================
-- PRIMEIRO ACESSO - CAMINHO 2 (SOMENTE CONVIDADO ENTRA)
-- ---------------------------------------------------------------------------
-- Objetivo:
--   1. Lista de convidados (allowed_emails): so quem o admin colocar aqui
--      consegue se cadastrar no "Primeiro acesso".
--   2. Gatilho que, ao nascer um usuario no Auth, confere se o e-mail esta
--      na lista. Se NAO estiver -> bloqueia o cadastro. Se estiver -> cria a
--      linha em public.profiles automaticamente (role padrao CLIENT) e marca
--      o convite como usado.
--
-- A trava fica no BANCO (nao so na tela do app): mesmo que alguem burle a
-- interface, o cadastro de um e-mail fora da lista e recusado pelo servidor.
-- ===========================================================================

create extension if not exists pgcrypto;

-- ---------------------------------------------------------------------------
-- 1. Lista de convidados
-- ---------------------------------------------------------------------------
create table if not exists public.allowed_emails (
    email       text primary key,
    name        text,
    role        text not null default 'CLIENT',
    used        boolean not null default false,
    invited_by  uuid references auth.users(id) on delete set null,
    created_at  timestamptz not null default now(),
    used_at     timestamptz
);

alter table public.allowed_emails enable row level security;

-- Somente ADMIN pode ver e gerenciar a lista de convidados.
drop policy if exists "Admins manage allowed emails" on public.allowed_emails;
create policy "Admins manage allowed emails"
on public.allowed_emails for all to authenticated
using (
    exists (
        select 1 from public.profiles
        where profiles.id = (select auth.uid())
          and profiles.role::text = 'ADMIN'
    )
)
with check (
    exists (
        select 1 from public.profiles
        where profiles.id = (select auth.uid())
          and profiles.role::text = 'ADMIN'
    )
);

-- ---------------------------------------------------------------------------
-- 2. Gatilho de primeiro acesso
--    Roda automaticamente quando um usuario novo e inserido em auth.users
--    (ou seja, no momento do signUp). Funciona como SECURITY DEFINER para
--    conseguir ler a lista e gravar em profiles sem depender do RLS.
-- ---------------------------------------------------------------------------
create or replace function public.handle_first_access()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_email   text := lower(new.email);
    v_invite  public.allowed_emails%rowtype;
begin
    -- Procura o e-mail na lista de convidados (nao sensivel a maiusculas).
    select * into v_invite
    from public.allowed_emails
    where lower(email) = v_email;

    -- Nao convidado -> barra o cadastro com uma mensagem clara.
    if not found then
        raise exception 'Este e-mail nao esta autorizado. Procure o administrador.'
            using errcode = 'check_violation';
    end if;

    -- Convite ja usado -> impede criar um segundo usuario com o mesmo convite.
    if v_invite.used then
        raise exception 'Este convite ja foi utilizado. Faca login normalmente.'
            using errcode = 'check_violation';
    end if;

    -- Convidado valido -> cria o perfil automaticamente.
    insert into public.profiles (id, email, name, role)
    values (
        new.id,
        new.email,
        coalesce(v_invite.name, split_part(new.email, '@', 1)),
        v_invite.role
    )
    on conflict (id) do nothing;

    -- Marca o convite como usado.
    update public.allowed_emails
    set used = true,
        used_at = now()
    where lower(email) = v_email;

    return new;
end;
$$;

-- (Re)instala o gatilho em auth.users.
drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_first_access();

-- ---------------------------------------------------------------------------
-- 3. Funcao auxiliar para o ADMIN cadastrar convites a partir do app
--    (opcional, mas evita que o app precise de permissao direta na tabela).
-- ---------------------------------------------------------------------------
create or replace function public.invite_email(
    p_email text,
    p_name  text default null,
    p_role  text default 'CLIENT'
)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
    if not exists (
        select 1 from public.profiles
        where id = (select auth.uid()) and role::text = 'ADMIN'
    ) then
        raise exception 'Apenas administradores podem convidar.';
    end if;

    insert into public.allowed_emails (email, name, role, invited_by)
    values (lower(p_email), p_name, upper(p_role), (select auth.uid()))
    on conflict (email) do update
        set name = excluded.name,
            role = excluded.role,
            used = false,
            used_at = null;
end;
$$;

notify pgrst, 'reload schema';
