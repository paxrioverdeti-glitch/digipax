-- Todo usuario logado precisa ENXERGAR os agendamentos de todos (tela "Agendados" e calendario),
-- para evitar conflito de horario. Escrever/alterar continua restrito (dono ou aprovador).
-- Politicas de SELECT somam (OR), entao esta nao quebra as outras.
alter table public.bookings enable row level security;

drop policy if exists "Authenticated users can view all bookings" on public.bookings;

create policy "Authenticated users can view all bookings"
on public.bookings for select to authenticated
using (true);

notify pgrst, 'reload schema';
