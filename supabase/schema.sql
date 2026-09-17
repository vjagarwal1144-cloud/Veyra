create extension if not exists pgcrypto;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.saved_places (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  latitude double precision not null check (latitude between -90 and 90),
  longitude double precision not null check (longitude between -180 and 180),
  created_at timestamptz not null default now()
);

create table if not exists public.journeys (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  destination_name text not null,
  latitude double precision not null check (latitude between -90 and 90),
  longitude double precision not null check (longitude between -180 and 180),
  wake_distance_meters real not null check (wake_distance_meters >= 50),
  transport text,
  protection text,
  fallback_alarm_at timestamptz,
  started_at timestamptz not null default now(),
  ended_at timestamptz,
  alarm_triggered boolean not null default false,
  created_at timestamptz not null default now()
);

create table if not exists public.journey_events (
  id uuid primary key default gen_random_uuid(),
  journey_id uuid not null references public.journeys(id) on delete cascade,
  event_type text not null,
  event_time timestamptz not null default now(),
  payload jsonb not null default '{}'::jsonb
);

create table if not exists public.device_tokens (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  fcm_token text not null,
  platform text,
  created_at timestamptz not null default now(),
  unique(user_id, fcm_token)
);

create index if not exists idx_saved_places_user on public.saved_places(user_id);
create index if not exists idx_journeys_user_started on public.journeys(user_id, started_at desc);
create index if not exists idx_events_journey_time on public.journey_events(journey_id, event_time desc);
create index if not exists idx_device_tokens_user on public.device_tokens(user_id);

alter table public.profiles enable row level security;
alter table public.saved_places enable row level security;
alter table public.journeys enable row level security;
alter table public.journey_events enable row level security;
alter table public.device_tokens enable row level security;

drop policy if exists "profiles own" on public.profiles;
drop policy if exists "places own" on public.saved_places;
drop policy if exists "journeys own" on public.journeys;
drop policy if exists "events own" on public.journey_events;
drop policy if exists "devices own" on public.device_tokens;

create policy "profiles own" on public.profiles
  for all using (auth.uid() = id) with check (auth.uid() = id);

create policy "places own" on public.saved_places
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

create policy "journeys own" on public.journeys
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

create policy "events own" on public.journey_events
  for all
  using (exists (select 1 from public.journeys j where j.id = journey_id and j.user_id = auth.uid()))
  with check (exists (select 1 from public.journeys j where j.id = journey_id and j.user_id = auth.uid()));

create policy "devices own" on public.device_tokens
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);
