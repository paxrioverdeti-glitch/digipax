-- Additive backend compatibility migration for the current mobile app.
-- It creates only missing objects and does not remove or rename existing data.

create extension if not exists pgcrypto;

-- ---------------------------------------------------------------------------
-- Tickets
-- ---------------------------------------------------------------------------
create table if not exists public.tickets (
    id uuid primary key default gen_random_uuid(),
    title text not null,
    description text not null,
    status text not null default 'na_fila',
    user_id uuid not null references auth.users(id) on delete cascade,
    user_name text not null,
    computer_name text not null default '',
    sector text not null default '',
    image_url text,
    created_at timestamptz not null default now(),
    rating integer,
    rating_comment text
);

alter table public.tickets
    add column if not exists user_id uuid,
    add column if not exists user_name text,
    add column if not exists computer_name text,
    add column if not exists image_url text,
    add column if not exists created_at timestamptz default now(),
    add column if not exists "createdAt" timestamptz,
    add column if not exists rating integer,
    add column if not exists rating_comment text;

create table if not exists public.ticket_comments (
    id uuid primary key default gen_random_uuid(),
    ticket_id uuid not null references public.tickets(id) on delete cascade,
    author_name text not null,
    text text not null,
    timestamp bigint not null,
    is_admin boolean not null default false
);

alter table public.tickets enable row level security;
alter table public.ticket_comments enable row level security;

drop policy if exists "Users can create their own tickets" on public.tickets;
create policy "Users can create their own tickets"
on public.tickets for insert to authenticated
with check (user_id::text = (select auth.uid())::text);

drop policy if exists "Users can read their own tickets" on public.tickets;
create policy "Users can read their own tickets"
on public.tickets for select to authenticated
using (
    user_id::text = (select auth.uid())::text
    or exists (
        select 1 from public.profiles
        where profiles.id = (select auth.uid())
          and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
);

drop policy if exists "Users and approvers can update tickets" on public.tickets;
create policy "Users and approvers can update tickets"
on public.tickets for update to authenticated
using (
    user_id::text = (select auth.uid())::text
    or exists (
        select 1 from public.profiles
        where profiles.id = (select auth.uid())
          and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
)
with check (
    user_id::text = (select auth.uid())::text
    or exists (
        select 1 from public.profiles
        where profiles.id = (select auth.uid())
          and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
);

drop policy if exists "Users can read ticket comments" on public.ticket_comments;
create policy "Users can read ticket comments"
on public.ticket_comments for select to authenticated
using (
    exists (
        select 1 from public.tickets
        where tickets.id = ticket_comments.ticket_id
          and (
              tickets.user_id::text = (select auth.uid())::text
              or exists (
                  select 1 from public.profiles
                  where profiles.id = (select auth.uid())
                    and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
              )
          )
    )
);

drop policy if exists "Authenticated users can create ticket comments" on public.ticket_comments;
create policy "Authenticated users can create ticket comments"
on public.ticket_comments for insert to authenticated
with check (
    exists (
        select 1 from public.tickets
        where tickets.id = ticket_comments.ticket_id
          and (
              tickets.user_id::text = (select auth.uid())::text
              or exists (
                  select 1 from public.profiles
                  where profiles.id = (select auth.uid())
                    and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
              )
          )
    )
);

-- Read views preserve the camelCase names expected by Kotlin serializers.
-- They are disposable read models, so recreate them when an older version
-- has a different column order.
drop view if exists public.tickets_api;

create or replace view public.tickets_api as
select
    t.id,
    t.title,
    t.description,
    t.status,
    t.user_id as "userId",
    t.user_name as "userName",
    t.computer_name,
    t.sector,
    coalesce(t."createdAt", t.created_at) as "createdAt",
    t.image_url,
    t.rating,
    t.rating_comment,
    p.avatar_url
from public.tickets t
left join public.profiles p on p.id = t.user_id;

alter view public.tickets_api set (security_invoker = true);

-- ---------------------------------------------------------------------------
-- Absences
-- ---------------------------------------------------------------------------
create table if not exists public.absences (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    user_name text not null,
    avatar_url text,
    sector text not null default '',
    target_approver_id uuid,
    target_approver_name text,
    type text not null,
    date text not null,
    expected_time text,
    effective_time text,
    exit_time text,
    return_time text,
    missing_hours text,
    missing_days text,
    original_time text,
    new_time text,
    reason text,
    admin_feedback jsonb,
    created_at bigint not null
);

-- Existing installations may have the table already, with only part of the
-- current schema. Add missing physical columns without changing old data.
alter table public.absences
    add column if not exists user_id uuid,
    add column if not exists user_name text,
    add column if not exists avatar_url text,
    add column if not exists target_approver_id uuid,
    add column if not exists target_approver_name text,
    add column if not exists expected_time text,
    add column if not exists effective_time text,
    add column if not exists exit_time text,
    add column if not exists return_time text,
    add column if not exists missing_hours text,
    add column if not exists missing_days text,
    add column if not exists original_time text,
    add column if not exists new_time text,
    add column if not exists admin_feedback jsonb,
    add column if not exists created_at bigint;

alter table public.absences enable row level security;

drop policy if exists "Users can create their own absences" on public.absences;
create policy "Users can create their own absences"
on public.absences for insert to authenticated
with check (user_id::text = (select auth.uid())::text);

drop policy if exists "Users and approvers can read absences" on public.absences;
create policy "Users and approvers can read absences"
on public.absences for select to authenticated
using (
    user_id::text = (select auth.uid())::text
    or target_approver_id::text = (select auth.uid())::text
    or exists (
        select 1 from public.profiles
        where profiles.id = (select auth.uid())
          and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
);

drop policy if exists "Approvers can update absences" on public.absences;
create policy "Approvers can update absences"
on public.absences for update to authenticated
using (
    target_approver_id::text = (select auth.uid())::text
    or exists (
        select 1 from public.profiles
        where profiles.id = (select auth.uid())
          and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
)
with check (true);

drop view if exists public.absences_api;

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
    a.created_at as "createdAt"
from public.absences a;

alter view public.absences_api set (security_invoker = true);

-- ---------------------------------------------------------------------------
-- Purchase requests
-- ---------------------------------------------------------------------------
create table if not exists public.purchase_requests (
    id uuid primary key default gen_random_uuid(),
    "requesterId" uuid not null references auth.users(id) on delete cascade,
    "requesterName" text not null,
    department text not null default '',
    "deliveryLocation" text not null default '',
    type text not null default 'MATERIAL',
    urgency text not null default 'BAIXA',
    "itemName" text not null,
    quantity text not null,
    justification text not null,
    "hasSuggestedVendor" boolean not null default false,
    "vendorName" text,
    "vendorContact" text,
    "technicalDescription" text not null default '',
    paymentmethod text not null default 'PIX',
    "estimatedValue" double precision,
    "approvedValue" double precision,
    "attachmentUrl" text,
    status text not null default 'PENDENTE',
    "managerComment" text,
    "createdAt" bigint not null default 0,
    "approvalDate" bigint,
    "approverName" text
);

alter table public.purchase_requests
    add column if not exists "requesterId" uuid,
    add column if not exists "requesterName" text,
    add column if not exists department text,
    add column if not exists "deliveryLocation" text,
    add column if not exists type text,
    add column if not exists urgency text,
    add column if not exists "itemName" text,
    add column if not exists quantity text,
    add column if not exists justification text,
    add column if not exists "hasSuggestedVendor" boolean default false,
    add column if not exists "vendorName" text,
    add column if not exists "vendorContact" text,
    add column if not exists "technicalDescription" text,
    add column if not exists paymentmethod text,
    add column if not exists "estimatedValue" double precision,
    add column if not exists "approvedValue" double precision,
    add column if not exists "attachmentUrl" text,
    add column if not exists status text,
    add column if not exists "managerComment" text,
    add column if not exists "createdAt" bigint default 0,
    add column if not exists "approvalDate" bigint,
    add column if not exists "approverName" text;

alter table public.purchase_requests enable row level security;

drop policy if exists "Users can create their own purchase requests" on public.purchase_requests;
create policy "Users can create their own purchase requests"
on public.purchase_requests for insert to authenticated
with check ("requesterId"::text = (select auth.uid())::text);

drop policy if exists "Users and approvers can read purchase requests" on public.purchase_requests;
create policy "Users and approvers can read purchase requests"
on public.purchase_requests for select to authenticated
using (
    "requesterId"::text = (select auth.uid())::text
    or exists (
        select 1 from public.profiles
        where profiles.id = (select auth.uid())
          and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
);

drop policy if exists "Approvers can update purchase requests" on public.purchase_requests;
create policy "Approvers can update purchase requests"
on public.purchase_requests for update to authenticated
using (
    exists (
        select 1 from public.profiles
        where profiles.id = (select auth.uid())
          and profiles.role::text in ('ADMIN', 'SUPERVISOR', 'ENCARREGADO')
    )
)
with check (true);

-- ---------------------------------------------------------------------------
-- Storage buckets used by the app
-- ---------------------------------------------------------------------------
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('attachments', 'attachments', true, 10485760,
        array['image/jpeg', 'image/png', 'image/webp', 'application/pdf']::text[])
on conflict (id) do update
set public = true,
    file_size_limit = 10485760,
    allowed_mime_types = excluded.allowed_mime_types;

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('profiles', 'profiles', true, 5242880,
        array['image/jpeg', 'image/png', 'image/webp']::text[])
on conflict (id) do update
set public = true,
    file_size_limit = 5242880,
    allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists "Authenticated users can upload attachments" on storage.objects;
create policy "Authenticated users can upload attachments"
on storage.objects for insert to authenticated
with check (bucket_id = 'attachments');

drop policy if exists "Public can read attachments" on storage.objects;
create policy "Public can read attachments"
on storage.objects for select to public
using (bucket_id = 'attachments');

drop policy if exists "Users can upload profile images" on storage.objects;
create policy "Users can upload profile images"
on storage.objects for insert to authenticated
with check (
    bucket_id = 'profiles'
    and (
        (storage.foldername(name))[1] = (select auth.uid())::text
        or name = (select auth.uid())::text || '.jpg'
    )
);

drop policy if exists "Public can read profile images" on storage.objects;
create policy "Public can read profile images"
on storage.objects for select to public
using (bucket_id = 'profiles');

-- ---------------------------------------------------------------------------
-- Realtime and schema cache
-- ---------------------------------------------------------------------------
do $$
declare
    table_name text;
begin
    foreach table_name in array array['tickets', 'ticket_comments', 'absences', 'purchase_requests'] loop
        if to_regclass('public.' || table_name) is not null
           and not exists (
               select 1
               from pg_publication_tables
               where pubname = 'supabase_realtime'
                 and schemaname = 'public'
                 and tablename = table_name
           ) then
            execute format('alter publication supabase_realtime add table public.%I', table_name);
        end if;
    end loop;
end $$;

notify pgrst, 'reload schema';
