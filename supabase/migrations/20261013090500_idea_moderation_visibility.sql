-- Keep unreviewed ideas visible only to their author and administrators.
drop policy if exists "Authenticated users can read ideas"
on public.ideas;

create policy "Authors admins and published ideas can be read"
on public.ideas
for select
to authenticated
using (
    author_id = (select auth.uid())
    or (
        status = 'APROVADO'
        and published_post_id is not null
    )
    or exists (
        select 1
        from public.profiles
        where profiles.id = (select auth.uid())
          and profiles.role::text = 'ADMIN'
    )
);

drop policy if exists "Admins can review ideas"
on public.ideas;

create policy "Admins can review ideas"
on public.ideas
for update
to authenticated
using (
    exists (
        select 1
        from public.profiles
        where profiles.id = (select auth.uid())
          and profiles.role::text = 'ADMIN'
    )
)
with check (
    exists (
        select 1
        from public.profiles
        where profiles.id = (select auth.uid())
          and profiles.role::text = 'ADMIN'
    )
);

create table if not exists public.idea_votes (
    idea_id uuid not null references public.ideas(id) on delete cascade,
    user_id uuid not null references auth.users(id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (idea_id, user_id)
);

alter table public.idea_votes enable row level security;

grant select on public.idea_votes to authenticated;

drop policy if exists "Users can read their own idea votes"
on public.idea_votes;

create policy "Users can read their own idea votes"
on public.idea_votes
for select
to authenticated
using (user_id = (select auth.uid()));

create or replace function public.vote_idea(p_idea_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := (select auth.uid());
    v_rows integer;
begin
    if v_user_id is null then
        raise exception 'E necessario estar autenticado para apoiar uma ideia.';
    end if;

    if not exists (
        select 1
        from public.ideas
        where id = p_idea_id
          and status = 'APROVADO'
          and published_post_id is not null
    ) then
        raise exception 'Somente ideias publicadas podem receber apoio.';
    end if;

    insert into public.idea_votes (idea_id, user_id)
    values (p_idea_id, v_user_id)
    on conflict (idea_id, user_id) do nothing;

    get diagnostics v_rows = row_count;
    if v_rows = 0 then
        raise exception 'Voce ja apoiou esta ideia.';
    end if;

    update public.ideas
    set votes_count = votes_count + 1
    where id = p_idea_id;
end;
$$;

revoke all on function public.vote_idea(uuid) from public;
grant execute on function public.vote_idea(uuid) to authenticated;

notify pgrst, 'reload schema';
