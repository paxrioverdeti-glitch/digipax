create extension if not exists pgcrypto;

create table if not exists public.feed_posts (
    id uuid primary key default gen_random_uuid(),
    title text not null,
    content text not null,
    category text not null default 'COMUNICADO',
    image_url text null,
    event_date text null,
    author_name text not null default 'Comunicação Interna',
    likes_count integer not null default 0,
    created_at timestamptz not null default now()
);

create table if not exists public.ideas (
    id uuid primary key default gen_random_uuid(),
    title text not null,
    description text not null,
    category text not null default 'MELHORIA',
    author_id uuid not null references auth.users(id) on delete cascade,
    author_name text not null,
    status text not null default 'PENDENTE',
    votes_count integer not null default 0,
    created_at timestamptz not null default now()
);

alter table public.ideas
    add column if not exists published_post_id uuid null,
    add column if not exists published_at timestamptz null,
    add column if not exists reviewed_by uuid null,
    add column if not exists reviewed_at timestamptz null;

alter table public.feed_posts
    add column if not exists image_url text null,
    add column if not exists event_date text null;

alter table public.ideas
    drop constraint if exists ideas_category_check;

alter table public.ideas
    add constraint ideas_category_check
    check (category in ('MELHORIA', 'INOVACAO', 'BEM_ESTAR', 'OUTROS'));

alter table public.ideas
    drop constraint if exists ideas_status_check;

alter table public.ideas
    add constraint ideas_status_check
    check (status in ('PENDENTE', 'APROVADO', 'REJEITADO'));

alter table public.feed_posts
    drop constraint if exists feed_posts_category_check;

alter table public.feed_posts
    add constraint feed_posts_category_check
    check (category in ('COMUNICADO', 'EVENTO', 'NOTICIA'));

alter table public.ideas
    drop constraint if exists ideas_published_post_id_fkey;

alter table public.ideas
    add constraint ideas_published_post_id_fkey
    foreign key (published_post_id)
    references public.feed_posts(id)
    on delete set null;

create index if not exists ideas_category_idx
    on public.ideas(category);

create index if not exists ideas_status_idx
    on public.ideas(status);

create index if not exists ideas_created_at_idx
    on public.ideas(created_at desc);

create index if not exists ideas_votes_count_idx
    on public.ideas(votes_count desc);

create index if not exists feed_posts_category_idx
    on public.feed_posts(category);

create index if not exists feed_posts_created_at_idx
    on public.feed_posts(created_at desc);

alter table public.ideas enable row level security;
alter table public.feed_posts enable row level security;

drop policy if exists "Authenticated users can read ideas"
on public.ideas;

create policy "Authenticated users can read ideas"
on public.ideas
for select
to authenticated
using (true);

drop policy if exists "Authenticated users can submit ideas"
on public.ideas;

create policy "Authenticated users can submit ideas"
on public.ideas
for insert
to authenticated
with check (auth.uid() = author_id);

drop policy if exists "Authenticated users can read feed posts"
on public.feed_posts;

create policy "Authenticated users can read feed posts"
on public.feed_posts
for select
to authenticated
using (true);

drop policy if exists "Admins can publish feed posts"
on public.feed_posts;

create policy "Admins can publish feed posts"
on public.feed_posts
for insert
to authenticated
with check (
    exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text = 'ADMIN'
    )
);
