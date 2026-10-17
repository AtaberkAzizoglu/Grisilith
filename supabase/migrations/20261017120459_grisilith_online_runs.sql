-- Grisilith beta backend v1. Hosted migration: 20261017120459, grisilith_online_runs.
-- All gameplay data is private. Clients can only call narrowly scoped RPCs.
begin;

create schema if not exists grisilith_private;
revoke all on schema grisilith_private from public, anon, authenticated;

create table grisilith_private.content_catalog (
  content_id text primary key,
  category text not null check(category in ('character','weapon','armor','item','tamga')),
  starting_allowed boolean not null
);
alter table grisilith_private.content_catalog enable row level security;

create table grisilith_private.profiles (
  player_id uuid primary key references auth.users(id) on delete cascade,
  display_name text,
  created_at timestamptz not null default now(),
  name_changed_at timestamptz,
  constraint valid_display_name check (display_name is null or display_name ~ '^[A-Za-z0-9_][A-Za-z0-9_-]{2,23}$')
);
create unique index profiles_unique_name on grisilith_private.profiles (lower(display_name));

create table grisilith_private.runs (
  run_id uuid primary key,
  player_id uuid not null references grisilith_private.profiles on delete cascade,
  map_id text not null check (map_id in ('ROAD_TO_HOME','TROUBLE_AT_HOME','FOLLOWING_THE_DUNGEON_DELVER')),
  difficulty text not null check (difficulty in ('EASY','NORMAL','HARD')),
  character_id text not null check (character_id in ('CHR_SIR_IMBERT','CHR_YWALETH_YOSTER','CHR_LAVENA_VOL','CHR_KOBA','CHR_LINLOS_DUSTMAN')),
  game_version text not null check (game_version = '0.3'),
  score_version integer not null default 1 check (score_version = 1),
  started_at timestamptz not null default now(),
  last_seen_at timestamptz not null default now(),
  finished_at timestamptz,
  status text not null default 'active' check (status in ('active','finished','ineligible')),
  reason text,
  starting_build jsonb not null,
  final_build jsonb,
  summary jsonb,
  stage_reached integer,
  duration_seconds integer,
  normal_kills integer,
  unique_kills integer,
  mini_boss_kills integer,
  final_boss_kills integer,
  score bigint,
  was_successful boolean
);
create index runs_player_history on grisilith_private.runs (player_id, started_at desc);
create index runs_leaderboard on grisilith_private.runs (map_id, score_version, score desc, finished_at, run_id)
  where status = 'finished';
create unique index runs_one_active_per_player on grisilith_private.runs (player_id) where status = 'active';
alter table grisilith_private.profiles enable row level security;
alter table grisilith_private.runs enable row level security;
revoke all on all tables in schema grisilith_private from public, anon, authenticated;

-- Private validators never trust client-supplied score or player ID.
create function grisilith_private.validate_build(b jsonb, starting boolean default false) returns void
language plpgsql set search_path = '' as $$
declare e jsonb; k text; v jsonb;
begin
  if b is null or jsonb_typeof(b) <> 'object' or octet_length(b::text) > 32768
     or jsonb_typeof(b->'weapons') is distinct from 'array'
     or jsonb_typeof(b->'items') is distinct from 'array'
     or jsonb_typeof(b->'tamgas') is distinct from 'array'
     or jsonb_typeof(b->'stats') is distinct from 'object' then
    raise exception 'invalid_build';
  end if;
  if jsonb_array_length(b->'weapons') not between (case when starting then 1 else 0 end) and 6
     or jsonb_array_length(b->'items') > 256 or jsonb_array_length(b->'tamgas') > 2 then
    raise exception 'invalid_build_size';
  end if;
  for e in select value from jsonb_array_elements(b->'weapons') loop
    if coalesce(e->>'id','') !~ '^WPN_[A-Z0-9_]{1,80}$'
       or jsonb_typeof(e->'level') is distinct from 'number'
       or coalesce(e->>'level','') !~ '^[1-5]$'
       or not exists(select 1 from grisilith_private.content_catalog where content_id=e->>'id' and category='weapon')
       then raise exception 'invalid_weapon'; end if;
  end loop;
  for e in select value from jsonb_array_elements(b->'items') loop
    if coalesce(e->>'id','') !~ '^ITEM_[A-Z0-9_]{1,80}$'
       or coalesce(e->>'count','') !~ '^[0-9]{1,4}$'
       or jsonb_typeof(e->'count') is distinct from 'number'
       or (e->>'count')::integer not between 1 and 1000
       or not exists(select 1 from grisilith_private.content_catalog where content_id=e->>'id' and category='item'
                     and (not starting or starting_allowed)) then raise exception 'invalid_item'; end if;
  end loop;
  for e in select value from jsonb_array_elements(b->'tamgas') loop
    if jsonb_typeof(e) <> 'string' or (e#>>'{}') !~ '^TMG_[A-Z0-9_]{1,80}$'
       or not exists(select 1 from grisilith_private.content_catalog where content_id=e#>>'{}' and category='tamga') then
      raise exception 'invalid_tamga';
    end if;
  end loop;
  if b->>'armor' is not null and (b->>'armor' !~ '^ARMOR_[A-Z0-9_]{1,80}$'
     or not exists(select 1 from grisilith_private.content_catalog where content_id=b->>'armor' and category='armor')) then
    raise exception 'invalid_armor';
  end if;
  if (select count(*) from jsonb_each(b->'stats')) > 32 then raise exception 'invalid_stats'; end if;
  for k,v in select * from jsonb_each(b->'stats') loop
    if k !~ '^[A-Z_]{1,40}$' or jsonb_typeof(v) <> 'number' or v::text !~ '^-?[0-9]{1,7}$' then
      raise exception 'invalid_stat';
    end if;
  end loop;
end $$;
revoke all on function grisilith_private.validate_build(jsonb,boolean) from public, anon, authenticated;

create function grisilith_private.api_status() returns jsonb
language sql stable security definer set search_path = '' as $$
  select jsonb_build_object('api_version',case when exists(select 1 from grisilith_private.content_catalog) then 1 else 0 end,
    'score_version',1,'game_version','0.3');
$$;

create function grisilith_private.profile(p_display_name text default null) returns jsonb
language plpgsql security definer set search_path = '' as $$
declare uid uuid := auth.uid(); p grisilith_private.profiles;
begin
  if uid is null then raise exception 'authentication_required'; end if;
  insert into grisilith_private.profiles(player_id) values(uid) on conflict do nothing;
  select * into p from grisilith_private.profiles where player_id=uid for update;
  if p_display_name is not null then
    if p_display_name !~ '^[A-Za-z0-9_][A-Za-z0-9_-]{2,23}$' then raise exception 'invalid_display_name'; end if;
    if p.display_name is distinct from p_display_name then
      if p.name_changed_at > now()-interval '1 minute' then raise exception 'name_change_rate_limit'; end if;
      update grisilith_private.profiles set display_name=p_display_name,name_changed_at=now() where player_id=uid;
    end if;
  end if;
  select * into p from grisilith_private.profiles where player_id=uid;
  return jsonb_build_object('player_id',p.player_id,'display_name',p.display_name);
exception when unique_violation then raise exception 'display_name_taken';
end $$;

create function grisilith_private.begin_run(p_run_id uuid, p_map_id text, p_difficulty text,
  p_character_id text, p_game_version text, p_starting_build jsonb) returns jsonb
language plpgsql security definer set search_path = '' as $$
declare uid uuid := auth.uid(); r grisilith_private.runs;
begin
  if uid is null then raise exception 'authentication_required'; end if;
  if p_run_id is null then raise exception 'invalid_run_id'; end if;
  perform grisilith_private.validate_build(p_starting_build,true);
  insert into grisilith_private.profiles(player_id) values(uid) on conflict do nothing;
  perform 1 from grisilith_private.profiles where player_id=uid for update;
  select * into r from grisilith_private.runs where run_id=p_run_id;
  if found then
    if r.player_id <> uid then raise exception 'run_not_found'; end if;
    return jsonb_build_object('run_id',r.run_id,'eligible',r.status='active' and r.last_seen_at>=now()-interval '30 seconds');
  end if;
  if exists(select 1 from grisilith_private.runs where player_id=uid and started_at>now()-interval '5 seconds') then
    raise exception 'run_start_rate_limit';
  end if;
  update grisilith_private.runs set status='ineligible',reason='superseded' where player_id=uid and status='active';
  insert into grisilith_private.runs(run_id,player_id,map_id,difficulty,character_id,game_version,starting_build)
    values(p_run_id,uid,p_map_id,p_difficulty,p_character_id,p_game_version,p_starting_build);
  return jsonb_build_object('run_id',p_run_id,'eligible',true);
end $$;

create function grisilith_private.heartbeat(p_run_id uuid, p_abandon boolean default false) returns jsonb
language plpgsql security definer set search_path = '' as $$
declare uid uuid := auth.uid(); r grisilith_private.runs;
begin
  if uid is null then raise exception 'authentication_required'; end if;
  select * into r from grisilith_private.runs where run_id=p_run_id and player_id=uid for update;
  if not found then raise exception 'run_not_found'; end if;
  if r.status='active' then
    if p_abandon or r.last_seen_at<now()-interval '30 seconds' or r.started_at<now()-interval '24 hours' then
      update grisilith_private.runs set status='ineligible',reason='connection_or_abandon' where run_id=p_run_id;
      return jsonb_build_object('eligible',false);
    end if;
    update grisilith_private.runs set last_seen_at=now() where run_id=p_run_id;
    return jsonb_build_object('eligible',true);
  end if;
  return jsonb_build_object('eligible',false);
end $$;

create function grisilith_private.finish_run(p_run_id uuid, p_result jsonb) returns jsonb
language plpgsql security definer set search_path = '' as $$
declare uid uuid := auth.uid(); r grisilith_private.runs; n bigint; u bigint; m bigint; f bigint;
  s integer; dur integer; max_stage integer; pct integer; computed bigint; won boolean; field text;
begin
  if uid is null then raise exception 'authentication_required'; end if;
  select * into r from grisilith_private.runs where run_id=p_run_id and player_id=uid for update;
  if not found then raise exception 'run_not_found'; end if;
  -- Idempotent: a repeated request returns the stored receipt, never another row.
  if r.status='finished' then return jsonb_build_object('accepted',true,'score',r.score); end if;
  if r.status<>'active' then return jsonb_build_object('accepted',false,'reason','ineligible'); end if;
  if r.last_seen_at<now()-interval '30 seconds' or r.started_at<now()-interval '24 hours' then
    update grisilith_private.runs set status='ineligible',reason='connection_timeout' where run_id=p_run_id;
    return jsonb_build_object('accepted',false,'reason','connection_timeout');
  end if;
  if p_result is null or jsonb_typeof(p_result)<>'object' or octet_length(p_result::text)>65536 then
    raise exception 'invalid_result';
  end if;
  foreach field in array array['normal_kills','unique_kills','mini_boss_kills','final_boss_kills','stage_reached','duration_seconds'] loop
    if jsonb_typeof(p_result->field) is distinct from 'number' or coalesce(p_result->>field,'') !~ '^[0-9]{1,9}$' then
      raise exception 'invalid_result_number';
    end if;
  end loop;
  if jsonb_typeof(p_result->'was_successful') is distinct from 'boolean'
     or jsonb_typeof(p_result->'summary') is distinct from 'object' then raise exception 'invalid_summary'; end if;
  n := (p_result->>'normal_kills')::bigint; u := (p_result->>'unique_kills')::bigint;
  m := (p_result->>'mini_boss_kills')::bigint; f := (p_result->>'final_boss_kills')::bigint;
  s := (p_result->>'stage_reached')::integer; dur := (p_result->>'duration_seconds')::integer;
  won := (p_result->>'was_successful')::boolean;
  max_stage := case r.difficulty when 'EASY' then 25 when 'NORMAL' then 35 else 50 end;
  pct := case r.difficulty when 'EASY' then 33 when 'NORMAL' then 50 else 100 end;
  -- Generous sanity bounds, NOT authoritative gameplay/anti-cheat simulation.
  if s not between 1 and max_stage or dur>172800 or dur>extract(epoch from(now()-r.started_at))*2+10
     -- Bonus mini-bosses can also be spawned by items; do not cap them at stage/5.
     or n+u+m+f>(dur::bigint+1)*1000 or f>1
     or (f=1 and s<>max_stage) or (won and (s<>max_stage or f<>1)) then raise exception 'implausible_result'; end if;
  perform grisilith_private.validate_build(p_result->'final_build');
  computed := ((n+u*3+m*250+f*5000)*pct*(10+s)+500)/1000;
  update grisilith_private.runs set status='finished',finished_at=now(),stage_reached=s,duration_seconds=dur,
    normal_kills=n,unique_kills=u,mini_boss_kills=m,final_boss_kills=f,score=computed,was_successful=won,
    final_build=p_result->'final_build',summary=p_result->'summary' where run_id=p_run_id;
  return jsonb_build_object('accepted',true,'score',computed);
end $$;

-- One best run per player, stable tie-breaking; no private builds/IDs are exposed.
create function grisilith_private.leaderboard(p_map_id text) returns jsonb
language plpgsql security definer set search_path = '' as $$
begin
  if auth.uid() is null then raise exception 'authentication_required'; end if;
  if p_map_id not in ('ROAD_TO_HOME','TROUBLE_AT_HOME','FOLLOWING_THE_DUNGEON_DELVER') or p_map_id is null then
    raise exception 'invalid_map';
  end if;
  return (select coalesce(jsonb_agg(to_jsonb(t)-'run_id' order by t.score desc,t.finished_at,t.run_id),'[]'::jsonb) from (
    select best.display_name,best.score,best.difficulty,best.character_id,best.stage_reached,best.finished_at,best.run_id
    from (
      select distinct on (r.player_id) p.display_name,r.score,r.difficulty,r.character_id,r.stage_reached,r.finished_at,r.run_id
      from grisilith_private.runs r join grisilith_private.profiles p using(player_id)
      where r.map_id=p_map_id and r.status='finished' and r.score_version=1 and p.display_name is not null
      order by r.player_id,r.score desc,r.finished_at,r.run_id
    ) best order by best.score desc,best.finished_at,best.run_id limit 10
  ) t);
end $$;

-- No SECURITY DEFINER code in an API-exposed schema. These wrappers run as the caller.
-- USAGE lets callers invoke specifically granted helpers, not read/write their tables.
revoke all on function grisilith_private.api_status() from public, anon, authenticated;
revoke all on function grisilith_private.profile(text) from public, anon, authenticated;
revoke all on function grisilith_private.begin_run(uuid,text,text,text,text,jsonb) from public, anon, authenticated;
revoke all on function grisilith_private.heartbeat(uuid,boolean) from public, anon, authenticated;
revoke all on function grisilith_private.finish_run(uuid,jsonb) from public, anon, authenticated;
revoke all on function grisilith_private.leaderboard(text) from public, anon, authenticated;
grant usage on schema grisilith_private to anon, authenticated;
grant execute on function grisilith_private.api_status() to anon, authenticated;
grant execute on function grisilith_private.profile(text) to authenticated;
grant execute on function grisilith_private.begin_run(uuid,text,text,text,text,jsonb) to authenticated;
grant execute on function grisilith_private.heartbeat(uuid,boolean) to authenticated;
grant execute on function grisilith_private.finish_run(uuid,jsonb) to authenticated;
grant execute on function grisilith_private.leaderboard(text) to authenticated;

create function public.grisilith_api_status() returns jsonb
language sql stable security invoker set search_path = '' as $$
  select grisilith_private.api_status();
$$;
create function public.grisilith_profile(p_display_name text default null) returns jsonb
language sql security invoker set search_path = '' as $$
  select grisilith_private.profile(p_display_name);
$$;
create function public.grisilith_begin_run(p_run_id uuid, p_map_id text, p_difficulty text,
  p_character_id text, p_game_version text, p_starting_build jsonb) returns jsonb
language sql security invoker set search_path = '' as $$
  select grisilith_private.begin_run(p_run_id,p_map_id,p_difficulty,p_character_id,p_game_version,p_starting_build);
$$;
create function public.grisilith_heartbeat(p_run_id uuid, p_abandon boolean default false) returns jsonb
language sql security invoker set search_path = '' as $$
  select grisilith_private.heartbeat(p_run_id,p_abandon);
$$;
create function public.grisilith_finish_run(p_run_id uuid, p_result jsonb) returns jsonb
language sql security invoker set search_path = '' as $$
  select grisilith_private.finish_run(p_run_id,p_result);
$$;
create function public.grisilith_leaderboard(p_map_id text) returns jsonb
language sql security invoker set search_path = '' as $$
  select grisilith_private.leaderboard(p_map_id);
$$;

revoke all on function public.grisilith_api_status() from public, anon, authenticated;
grant execute on function public.grisilith_api_status() to anon, authenticated;
revoke all on function public.grisilith_profile(text) from public, anon, authenticated;
revoke all on function public.grisilith_begin_run(uuid,text,text,text,text,jsonb) from public, anon, authenticated;
revoke all on function public.grisilith_heartbeat(uuid,boolean) from public, anon, authenticated;
revoke all on function public.grisilith_finish_run(uuid,jsonb) from public, anon, authenticated;
revoke all on function public.grisilith_leaderboard(text) from public, anon, authenticated;
grant execute on function public.grisilith_profile(text) to authenticated;
grant execute on function public.grisilith_begin_run(uuid,text,text,text,text,jsonb) to authenticated;
grant execute on function public.grisilith_heartbeat(uuid,boolean) to authenticated;
grant execute on function public.grisilith_finish_run(uuid,jsonb) to authenticated;
grant execute on function public.grisilith_leaderboard(text) to authenticated;

commit;
