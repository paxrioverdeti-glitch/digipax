insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
    'trip_photos',
    'trip_photos',
    true,
    10485760,
    array['image/jpeg', 'image/png', 'image/webp']::text[]
)
on conflict (id) do update
set public = true,
    file_size_limit = 10485760,
    allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists "Authenticated users can upload trip photos" on storage.objects;
create policy "Authenticated users can upload trip photos"
on storage.objects for insert to authenticated
with check (bucket_id = 'trip_photos');

drop policy if exists "Authenticated users can update trip photos" on storage.objects;
create policy "Authenticated users can update trip photos"
on storage.objects for update to authenticated
using (bucket_id = 'trip_photos')
with check (bucket_id = 'trip_photos');

drop policy if exists "Public can read trip photos" on storage.objects;
create policy "Public can read trip photos"
on storage.objects for select
to public
using (bucket_id = 'trip_photos');
