begin;

-- Explicit alphabet mapping avoids server collation and Turkish-locale surprises.
create function grisilith_private.name_upper(v text) returns text
language sql immutable strict set search_path='' as $$
  select translate(v,'abcdefghijklmnopqrstuvwxyzçğıöşü','ABCDEFGHIJKLMNOPQRSTUVWXYZÇĞIÖŞÜ');
$$;
create function grisilith_private.name_filter_text(v text) returns text
language sql immutable strict set search_path='' as $$
  select trim(regexp_replace(regexp_replace(replace(grisilith_private.name_upper(v),'İ','I'),
    '[^A-Z0-9ÇĞÖŞÜ ]','','g'),' +',' ','g'));
$$;

-- Filled from assets/online/name-policy.json by the checked-in synchronization tool.
create function grisilith_private.name_policy() returns jsonb
language sql immutable set search_path='' as $$ select '{"version":1,"banned":["FUCK","SHIT","COCK","DICK","CUNT","TWAT","WANK","WANKER","WANKING","BOLLOCKS","BOLLOCK","ASSHOLE","ARSEHOLE","DUMBASS","JACKASS","BADASS","SMARTASS","ASSHAT","ASSWIPE","ASSFACE","ASSKISSER","BITCH","BASTARD","SCUMBAG","SHITHEEL","PUSSY","SİK","SİKMEK","SİKTİR","SİKTİRİK","SİKTİRİP","SİKTİĞİM","SİKTİĞİMİN","SİKEYİM","SİKERİM","SİKİŞ","SİKİŞMEK","SİKİK","SİKİCİ","SİKİLEN","AMCIK","AMCIĞI","AMCIĞIN","AMINA","AMINI","AMINAKOYAYIM","AMINAKOYİM","AMINAKOYUM","AMINAKODUM","AMINAKODUĞUM","ANANISİK","ANANISİKEYİM","ANANISİKERİM","ANANINAMINA","BACINISİK","BACINISİKEYİM","YARRAK","YARRAĞI","YARRAĞIM","YARRAĞIN","YARRAĞIMI","YARRAKLI","YARRAM","YARAK","GÖT","GÖTÜ","GÖTÜN","GÖTÜM","GÖTÜNE","GÖTÜNÜ","GÖTVEREN","GÖTLEK","GÖTOŞ","GÖTLALESİ","OROSPU","OROSPUÇOCUĞU","OROSPUCOCUGU","OROSPUEVLADI","OROSPULUK","OROSPUÇOCUKLARI","PİÇ","PİÇKURUSU","PİÇİN","PİÇLİK","PEZEVENK","PEZEVENKLİK","KAHPE","KAHPELİK","KALTAK","OÇ","O C","OC","O.Ç","O-C","O_C","O COCUGU","SKTR","S1KT1R","S1K","SİQ","SİQMEK","PORN","PORNO","PORNOGRAPHY","PORNHUB","XXX","NSFW","HENTAI","PENIS","VAGINA","VULVA","CLIT","CLITORIS","SCROTUM","TESTICLE","TESTICLES","ANUS","BALLS","BOOB","BOOBS","CUM","CUMSHOT","JIZZ","SEMEN","SPERM","ORGASM","ERECTION","EJACULATE","MASTURBATE","MASTURBATION","BLOWJOB","HANDJOB","RIMJOB","DEEPTHROAT","GANGBANG","CREAMPIE","BUKKAKE","FISTING","ANAL","BDSM","INCEST","WHORE","SLUT","HOOKER","HOE","HO","SKANK","PORNOGRAFİ","SEKS","SEKSİ","CİNSELLİK","ÇIPLAK","ÇIPLAKLIK","PENİS","VAJİNA","KLİTORİS","TESTİS","TESTİSLER","ANÜS","MEME","MEMELER","TAŞŞAK","TAŞAK","TAŞAKLAR","DÖL","MENİ","BOŞALMA","BOŞALMAK","ORGAZM","EREKSİYON","MASTÜRBASYON","MASTURBASYON","OTUZBİR","OTUZ BİR","SAKSO","ENSEST","NIGGER","NIGGA","FAGGOT","FAG","DYKE","TRANNY","KIKE","SPIC","CHINK","GOOK","WETBACK","PAKI","RAGHEAD","TOWELHEAD","RETARD","RETARDED","SPASTIC","SPAZ","NAZI","NEONAZI","HITLER","HEILHITLER","SIEGHEIL","İBNE","İPNE","PUŞT","NONOŞ","KILLYOURSELF","GO KILL YOURSELF","KYS","HANGYOURSELF","I WILL KILL YOU","IWILLKILLYOU","YOU SHOULD DIE","YOUSHOULDDIE","KENDİNİÖLDÜR","KENDİNİ ÖLDÜR","İNTİHARET","İNTİHAR ET","KENDİNİAS","KENDİNİ AS","SENİÖLDÜRÜRÜM","SENİ ÖLDÜRÜRÜM","ÖLMELİSİN"],"whitelist":["PEACOCK","PEACOCKS","COCKPIT","COCKTAIL","COCKEREL","COCKATOO","SHUTTLECOCK","WOODCOCK","HANCOCK","HITCHCOCK","DICKENS","DICKINSON","DICKSON","DOCUMENT","DOCUMENTARY","DOCUMENTATION","ACCUMULATE","ACCUMULATION","CUMULUS","CUMULATIVE","CIRCUMSTANCE","CIRCUMFERENCE","CIRCUMVENT","SUCCUMB","CUCUMBER","ANALYSIS","ANALYSES","ANALYST","ANALYTIC","ANALYTICS","ANALOG","ANALOGUE","CANAL","BANAL","KANAL","ANALİZ","ANALİTİK","ANALOJİ","ANALOG","THOR","PHOENIX","HONOR","HONOUR","HOUSE","HOPE","CHOIR","HORIZON","HOLLOW","HORSE","HORSEMAN","HOUND","HONEY","HOBBIT","HORN","HOSPITAL","HOST","GHOST","SCHOOL","SCHOLAR","ANCHOR","AUTHOR","SHADOW","THORN","HOUR","HOLIDAY","HOLY","HEROHOOD","CHOSEN","SHOES","SHOE","SHOOT","SHORT","SHOP","SHOW","ECHO","PSYCHOLOGY","PSYCHOLOGIST","OCEAN","OCEANIC","DOCTOR","ROCK","ROCKET","FOCUS","LOCAL","LOCATION","SOCIAL","SOCIETY","PROCESS","BLOCK","CLOCK","CHOCOLATE","DOCUMENT","VOCAL","OCTOPUS","OCTOBER","OCTAGON","SOCCER","SOCKET","POCKET","LOCK","DOCK","FLOCK","STOCK","BROCCOLI","PROCLAIM","OCCASION","INNOCENT","MOCK","ADVOCATE","EPOCH","COCONUT","VOCATION","ROCHELLE","ROCHESTER","STAMINA","LAMINA","LAMINATE","VITAMINI","VİTAMİNİ","TAMINA","BİSİKLET","BİSİKLETÇİ","PSİKOLOJİ","PSİKOLOG","PSİKİYATRİ","ANSİKLOPEDİ","KLASİK","MUSİKİ","SİKLON","SİKKE","EKSİK","KESİK","FİZİKSEL","ERMENİ","ERMENİCE","SKYSCRAPER","SKYSHIP","SPICE","SPICY","SPICED","SUSPICIOUS","CONSPICUOUS","AUSPICIOUS","RETARDANT","NAZİK","NAZİRE","NAZİFE","NAZİLLİ","SAKSOFON","SAKSOFONCU","MEMELİ","MEMELİLER","GÖTÜR","GÖTÜRMEK","GÖTÜRÜ","FIREBALLS","SNOWBALLS","EYEBALLS","URANUS","JANUS","ROMANUS","OCEANUS","TOCCATA","FOÇA","KOCA","KOCAMAN","HOCAM","HOCA","HOŞ","HOŞGÖRÜ","HOKKABAZ","HOŞGELDİN","KOCATEPE","SOKAK","PAKİSTAN"]}'::jsonb $$;

create function grisilith_private.name_blocked(v text) returns boolean
language plpgsql immutable set search_path='' as $$
declare policy jsonb:=grisilith_private.name_policy(); txt text:=grisilith_private.name_filter_text(v);
  candidates text[]; part text; short_run text:=''; candidate text; bad text; safe text;
  at_pos integer; from_pos integer; safe_pos integer; safe_from integer; covered boolean;
begin
  candidates:=array[txt];
  foreach part in array string_to_array(txt,' ') loop
    if length(part) between 1 and 2 then short_run:=short_run||part;
    else
      if short_run<>'' then candidates:=array_append(candidates,short_run); short_run:=''; end if;
    end if;
  end loop;
  if short_run<>'' then candidates:=array_append(candidates,short_run); end if;
  foreach candidate in array candidates loop
    for bad in select distinct grisilith_private.name_filter_text(value) from jsonb_array_elements_text(policy->'banned') loop
      from_pos:=1;
      loop
        at_pos:=strpos(substr(candidate,from_pos),bad);
        exit when at_pos=0;
        at_pos:=at_pos+from_pos-1; covered:=false;
        for safe in select distinct grisilith_private.name_filter_text(value) from jsonb_array_elements_text(policy->'whitelist') loop
          safe_from:=1;
          loop
            safe_pos:=strpos(substr(candidate,safe_from),safe);
            exit when safe_pos=0;
            safe_pos:=safe_pos+safe_from-1;
            if (safe_pos=1 or substr(candidate,safe_pos-1,1)!~'[A-ZÇĞÖŞÜ]')
               and (safe_pos+length(safe)>length(candidate) or substr(candidate,safe_pos+length(safe),1)!~'[A-ZÇĞÖŞÜ]')
               and at_pos>=safe_pos and at_pos+length(bad)<=safe_pos+length(safe) then covered:=true; exit; end if;
            safe_from:=safe_pos+1;
          end loop;
          exit when covered;
        end loop;
        if not covered then return true; end if;
        from_pos:=at_pos+1;
      end loop;
    end loop;
  end loop;
  return false;
end $$;

alter table grisilith_private.profiles add column name_confirmed boolean not null default false;
alter table grisilith_private.profiles drop constraint valid_display_name;
drop index grisilith_private.profiles_unique_name;
-- Existing selections count as completed onboarding; default UUIDs are never stored as custom names.
update grisilith_private.profiles set display_name=grisilith_private.name_upper(display_name),
  name_confirmed=(display_name is not null);
create unique index profiles_unique_name on grisilith_private.profiles ((display_name collate "C"));
alter table grisilith_private.profiles add constraint valid_display_name check (
  display_name is null or (length(display_name) between 3 and 24
    and display_name=grisilith_private.name_upper(display_name)
    and translate(display_name,$chars$ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ÇĞİÖŞÜ ._-|!+()[]#@,;:/*"'$chars$,'')=''
    and translate(display_name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ÇĞİÖŞÜ','')<>display_name));

create or replace function grisilith_private.profile(p_display_name text default null) returns jsonb
language plpgsql security definer set search_path='' as $$
declare uid uuid:=auth.uid(); p grisilith_private.profiles; wanted text;
begin
  if uid is null then raise exception 'authentication_required'; end if;
  insert into grisilith_private.profiles(player_id) values(uid) on conflict do nothing;
  select * into p from grisilith_private.profiles where player_id=uid for update;
  if p_display_name is not null then
    if octet_length(p_display_name)>256 then raise exception 'invalid_display_name'; end if;
    wanted:=trim(grisilith_private.name_upper(p_display_name));
    if wanted='' or wanted=grisilith_private.name_upper(uid::text) then wanted:=null;
    else
      if length(wanted) not between 3 and 24
         or translate(wanted,$chars$ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ÇĞİÖŞÜ ._-|!+()[]#@,;:/*"'$chars$,'')<>''
         or translate(wanted,'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ÇĞİÖŞÜ','')=wanted then raise exception 'invalid_display_name'; end if;
      if grisilith_private.name_blocked(wanted) then raise exception 'blocked_display_name'; end if;
    end if;
    update grisilith_private.profiles set display_name=wanted,name_confirmed=true,
      name_changed_at=case when display_name is distinct from wanted then now() else name_changed_at end where player_id=uid;
  end if;
  select * into p from grisilith_private.profiles where player_id=uid;
  return jsonb_build_object('player_id',uid,'display_name',coalesce(p.display_name,grisilith_private.name_upper(uid::text)),
    'name_confirmed',p.name_confirmed,'name_policy_version',1);
exception when unique_violation then raise exception 'display_name_taken';
end $$;

create index runs_global_leaderboard on grisilith_private.runs(score desc,duration_seconds,finished_at,run_id)
  where status='finished' and score_version=1;
create index runs_personal_best on grisilith_private.runs(player_id,score desc,duration_seconds,finished_at,run_id)
  where status='finished' and score_version=1;

create function grisilith_private.name_available(p_name text) returns jsonb
language plpgsql stable security definer set search_path='' as $$
declare uid uuid:=auth.uid(); wanted text:=trim(grisilith_private.name_upper(p_name));
begin
  if uid is null then raise exception 'authentication_required'; end if;
  if p_name is null or octet_length(p_name)>256 then raise exception 'invalid_display_name'; end if;
  if wanted='' or wanted=grisilith_private.name_upper(uid::text) then return jsonb_build_object('available',true); end if;
  if length(wanted) not between 3 and 24
    or translate(wanted,$chars$ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ÇĞİÖŞÜ ._-|!+()[]#@,;:/*"'$chars$,'')<>''
    or translate(wanted,'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ÇĞİÖŞÜ','')=wanted then raise exception 'invalid_display_name'; end if;
  if grisilith_private.name_blocked(wanted) then raise exception 'blocked_display_name'; end if;
  return jsonb_build_object('available',not exists(select 1 from grisilith_private.profiles
    where display_name=wanted collate "C" and player_id<>uid));
end $$;
create function public.grisilith_name_available(p_name text) returns jsonb
language sql stable security invoker set search_path='' as $$ select grisilith_private.name_available(p_name) $$;

create function grisilith_private.leaderboard_all() returns jsonb
language plpgsql stable security definer set search_path='' as $$
declare uid uuid:=auth.uid(); output jsonb;
begin
  if uid is null then raise exception 'authentication_required'; end if;
  with top_eleven as materialized (
    select r.* from grisilith_private.runs r where status='finished' and score_version=1
    order by score desc,duration_seconds,finished_at,run_id limit 11
  ), top_ten as (select * from top_eleven order by score desc,duration_seconds,finished_at,run_id limit 10),
  personal as (
    select r.* from grisilith_private.runs r where player_id=uid and status='finished' and score_version=1
    order by score desc,duration_seconds,finished_at,run_id limit 1
  ), footer as (
    select * from top_eleven where exists(select 1 from top_ten where player_id=uid)
    order by score desc,duration_seconds,finished_at,run_id offset 10 limit 1
  ), picked as (
    select 'top' as kind,t.* from top_ten t
    union all select 'next',f.* from footer f
    union all select 'personal',p.* from personal p where not exists(select 1 from top_ten where player_id=uid)
  ), entries as (
    select kind,r.score,r.duration_seconds,r.finished_at,r.run_id,
      jsonb_build_object('rank',1+(select count(*) from grisilith_private.runs x
        where x.status='finished' and x.score_version=1 and x.score>r.score),
        'display_name',coalesce(p.display_name,grisilith_private.name_upper(r.player_id::text)),
        'score',r.score,'duration_seconds',r.duration_seconds,'character_id',r.character_id,
        'map_id',r.map_id,'is_self',r.player_id=uid) as entry
    from picked r join grisilith_private.profiles p using(player_id)
  ) select jsonb_build_object(
    'top',coalesce((select jsonb_agg(entry order by score desc,duration_seconds,finished_at,run_id) from entries where kind='top'),'[]'::jsonb),
    'footer',(select entry from entries where kind<>'top' limit 1),
    'footer_kind',case when exists(select 1 from top_ten where player_id=uid) then 'next' else 'personal' end
  ) into output;
  return output;
end $$;
create function public.grisilith_leaderboard_all() returns jsonb
language sql stable security invoker set search_path='' as $$ select grisilith_private.leaderboard_all() $$;

revoke all on function grisilith_private.name_upper(text) from public,anon,authenticated;
revoke all on function grisilith_private.name_filter_text(text) from public,anon,authenticated;
revoke all on function grisilith_private.name_policy() from public,anon,authenticated;
revoke all on function grisilith_private.name_blocked(text) from public,anon,authenticated;
revoke all on function grisilith_private.leaderboard_all() from public,anon,authenticated;
revoke all on function public.grisilith_leaderboard_all() from public,anon,authenticated;
revoke all on function grisilith_private.name_available(text) from public,anon,authenticated;
revoke all on function public.grisilith_name_available(text) from public,anon,authenticated;
grant execute on function grisilith_private.name_available(text) to authenticated;
grant execute on function public.grisilith_name_available(text) to authenticated;
grant execute on function grisilith_private.leaderboard_all() to authenticated;
grant execute on function public.grisilith_leaderboard_all() to authenticated;
commit;
