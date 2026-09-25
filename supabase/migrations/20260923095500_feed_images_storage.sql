insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
    'feed_images',
    'feed_images',
    true,
    10485760,
    array['image/jpeg', 'image/png', 'image/webp']::text[]
)
on conflict (id) do update
set public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists "Authenticated users can view feed images"
on storage.objects;

create policy "Authenticated users can view feed images"
on storage.objects
for select
to authenticated
using (bucket_id = 'feed_images');

drop policy if exists "Admins can upload feed images"
on storage.objects;

create policy "Admins can upload feed images"
on storage.objects
for insert
to authenticated
with check (
    bucket_id = 'feed_images'
    and exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text = 'ADMIN'
    )
);

drop policy if exists "Admins can update feed images"
on storage.objects;

create policy "Admins can update feed images"
on storage.objects
for update
to authenticated
using (
    bucket_id = 'feed_images'
    and exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text = 'ADMIN'
    )
)
with check (
    bucket_id = 'feed_images'
    and exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text = 'ADMIN'
    )
);

drop policy if exists "Admins can delete feed images"
on storage.objects;

create policy "Admins can delete feed images"
on storage.objects
for delete
to authenticated
using (
    bucket_id = 'feed_images'
    and exists (
        select 1
        from public.profiles
        where profiles.id = auth.uid()
          and profiles.role::text = 'ADMIN'
    )
);
