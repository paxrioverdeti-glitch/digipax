insert into storage.buckets (id, name, public)
values ('trip_photos', 'trip_photos', true)
on conflict (id) do update set public = true;

drop policy if exists "Public can read trip photos" on storage.objects;
create policy "Public can read trip photos"
on storage.objects
for select
to public
using (bucket_id = 'trip_photos');

notify pgrst, 'reload schema';
