-- Applied and recorded on the hosted Grisilith project, 2026-10-20.
begin;
create table grisilith_private.run_praises (
  run_id uuid not null references grisilith_private.runs(run_id) on delete cascade,
  player_id uuid not null references grisilith_private.profiles(player_id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key(run_id,player_id)
);
create index run_praises_player_idx on grisilith_private.run_praises(player_id);
alter table grisilith_private.run_praises enable row level security;
revoke all on grisilith_private.run_praises from public,anon,authenticated;

-- Serialize top-ten changes and votes before either operation reads eligibility.
create function grisilith_private.lock_leaderboard() returns trigger
language plpgsql security definer set search_path='' as $$
begin perform pg_catalog.pg_advisory_xact_lock(827310,10); return null; end $$;
create function grisilith_private.prune_praises() returns trigger
language plpgsql security definer set search_path='' as $$
begin
 delete from grisilith_private.run_praises p where not exists (
   select 1 from (
    select r.run_id from grisilith_private.runs r where r.status='finished' and r.score_version=1
    order by r.score desc,r.duration_seconds,r.finished_at,r.run_id limit 10
   ) top_ten where top_ten.run_id=p.run_id
 );
 return null;
end $$;
create trigger leaderboard_lock before insert or delete or update of status,score,score_version,duration_seconds,finished_at,run_id
on grisilith_private.runs for each statement execute function grisilith_private.lock_leaderboard();
create trigger leaderboard_prune after insert or delete or update of status,score,score_version,duration_seconds,finished_at,run_id
on grisilith_private.runs for each statement execute function grisilith_private.prune_praises();

create function grisilith_private.praise(p_run_id uuid) returns jsonb
language plpgsql security definer set search_path='' as $$
declare uid uuid:=auth.uid(); owner_id uuid; total bigint;
begin
 if uid is null then raise exception 'authentication_required'; end if;
 perform pg_catalog.pg_advisory_xact_lock(827310,10);
 select t.player_id into owner_id from (
  select r.run_id,r.player_id from grisilith_private.runs r where r.status='finished' and r.score_version=1
  order by r.score desc,r.duration_seconds,r.finished_at,r.run_id limit 10
 ) t where t.run_id=p_run_id;
 if owner_id is null then raise exception 'run_not_in_top10'; end if;
 if owner_id=uid then raise exception 'cannot_praise_own_run'; end if;
 insert into grisilith_private.run_praises(run_id,player_id) values(p_run_id,uid) on conflict do nothing;
 select count(*) into total from grisilith_private.run_praises p where p.run_id=p_run_id;
 return jsonb_build_object('accepted',true,'praise_count',total);
end $$;
create function public.grisilith_praise(p_run_id uuid) returns jsonb
language sql security invoker set search_path='' as $$select grisilith_private.praise(p_run_id)$$;
revoke all on function grisilith_private.lock_leaderboard() from public,anon,authenticated;
revoke all on function grisilith_private.prune_praises() from public,anon,authenticated;
revoke all on function grisilith_private.praise(uuid) from public,anon,authenticated;
revoke all on function public.grisilith_praise(uuid) from public,anon,authenticated;
grant execute on function grisilith_private.praise(uuid) to authenticated;
grant execute on function public.grisilith_praise(uuid) to authenticated;

create or replace function grisilith_private.leaderboard_all() returns jsonb
language plpgsql stable security definer set search_path='' as $$
declare uid uuid:=auth.uid(); output jsonb;
begin
 if uid is null then raise exception 'authentication_required'; end if;
 with top_eleven as materialized (
  select r.* from grisilith_private.runs r where r.status='finished' and r.score_version=1
  order by score desc,duration_seconds,finished_at,run_id limit 11
 ),top_ten as (
  select * from top_eleven order by score desc,duration_seconds,finished_at,run_id limit 10
 ),personal as (
  select r.* from grisilith_private.runs r where player_id=uid and status='finished' and score_version=1
  order by score desc,duration_seconds,finished_at,run_id limit 1
 ),footer as (
  select * from top_eleven where exists(select 1 from top_ten where player_id=uid)
  order by score desc,duration_seconds,finished_at,run_id offset 10 limit 1
 ),picked as (
  select 'top' as kind,t.* from top_ten t
  union all select 'next',f.* from footer f
  union all select 'personal',p.* from personal p where not exists(select 1 from top_ten where player_id=uid)
 ),entries as (
  select kind,r.score,r.duration_seconds,r.finished_at,r.run_id,
   jsonb_build_object(
    'run_id',r.run_id,
    'rank',1+(select count(*) from grisilith_private.runs x where x.status='finished' and x.score_version=1 and x.score>r.score),
    'display_name',coalesce(p.display_name,grisilith_private.name_upper(r.player_id::text)),
    'score',r.score,'duration_seconds',r.duration_seconds,'character_id',r.character_id,
    'map_id',r.map_id,'is_self',r.player_id=uid,
    'praise_count',(select count(*) from grisilith_private.run_praises v where v.run_id=r.run_id),
    'praised_by_me',exists(select 1 from grisilith_private.run_praises v where v.run_id=r.run_id and v.player_id=uid),
    'can_praise',kind='top' and r.player_id<>uid and not exists(select 1 from grisilith_private.run_praises v where v.run_id=r.run_id and v.player_id=uid)
   ) as entry
  from picked r join grisilith_private.profiles p using(player_id)
 )
 select jsonb_build_object(
  'top',coalesce((select jsonb_agg(entry order by score desc,duration_seconds,finished_at,run_id) from entries where kind='top'),'[]'::jsonb),
  'footer',(select entry from entries where kind<>'top' limit 1),
  'footer_kind',case when exists(select 1 from top_ten where player_id=uid) then 'next' else 'personal' end
 ) into output;
 return output;
end $$;
commit;

