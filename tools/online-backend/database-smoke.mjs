// Local, in-memory PostgreSQL only. Never connects to the hosted Supabase project.
// Test dependency: @electric-sql/pglite 0.5.10 extracted under runtime/package.
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { PGlite } from './runtime/package/dist/index.js';

const db = new PGlite();
const migration = name => readFileSync(new URL(`../../supabase/migrations/${name}`, import.meta.url), 'utf8');
const one = async (sql, args = []) => (await db.query(sql, args)).rows[0];
const a = '00000000-0000-4000-8000-000000000001';
const b = '00000000-0000-4000-8000-000000000002';
const run1 = '10000000-0000-4000-8000-000000000001';
const run2 = '10000000-0000-4000-8000-000000000002';
const run3 = '10000000-0000-4000-8000-000000000003';
const build = { weapons: [{id:'WPN_LONGSWORD',level:1}], armor:'ARMOR_LEATHER', tamgas:['TMG_CHARGER'], items:[], stats:{STRENGTH:12} };
const result = { normal_kills:100, unique_kills:3, mini_boss_kills:3, final_boss_kills:0,
  stage_reached:17, duration_seconds:1000, was_successful:false, final_build:build, summary:{damage_dealt:1000} };
const owner = () => db.exec('reset role');
async function auth(id) {
  await db.exec('reset role; set role authenticated;');
  await db.query("select set_config('request.jwt.claim.sub',$1,false)",[id]);
}
const begin = (id, map='ROAD_TO_HOME', difficulty='HARD', payload=build) =>
  one('select public.grisilith_begin_run($1,$2,$3,$4,$5,$6::jsonb) as receipt',
    [id,map,difficulty,'CHR_KOBA','0.3',JSON.stringify(payload)]);
const finish = (id, payload=result) => one('select public.grisilith_finish_run($1,$2::jsonb) as receipt',[id,JSON.stringify(payload)]);
async function ageRun(id, seconds=600) {
  await owner();
  await db.query("update grisilith_private.runs set started_at=now()-make_interval(secs=>$2::integer),last_seen_at=now() where run_id=$1",[id,seconds]);
}

try {
  // Minimal Supabase auth scaffolding; migrations run unchanged against real PostgreSQL.
  await db.exec(`create role anon; create role authenticated; create schema auth;
    create table auth.users(id uuid primary key);
    create function auth.uid() returns uuid language sql stable as
    $$ select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid $$;
    grant usage on schema auth to anon,authenticated;
    insert into auth.users values('${a}'),('${b}');`);
  await db.exec(migration('20261017120459_grisilith_online_runs.sql'));
  assert.equal((await one('select public.grisilith_api_status() as api')).api.api_version,0);
  await db.exec(migration('20261017120640_grisilith_content_catalog.sql'));
  assert.equal((await one('select public.grisilith_api_status() as api')).api.api_version,1);
  assert.equal((await one('select count(*)::integer as count from grisilith_private.content_catalog')).count,130);
  // RLS and grants: no direct table reads/writes, even on one's own rows.
  const tables = (await db.query("select relname,relrowsecurity from pg_class join pg_namespace n on n.oid=relnamespace where n.nspname='grisilith_private' and relkind='r'")).rows;
  assert(tables.every(t => t.relrowsecurity));
  await db.exec('set role anon;');
  await db.query("select set_config('request.jwt.claim.sub','',false)");
  await assert.rejects(one('select public.grisilith_profile()'), /permission denied/);
  assert.equal((await one('select public.grisilith_api_status() as api')).api.api_version,1);
  await assert.rejects(one('select * from grisilith_private.runs'), /permission denied/);

  await auth(a);
  await one("select public.grisilith_profile('Alice')");
  await assert.rejects(one("select public.grisilith_profile('ab')"), /invalid_display_name/);
  await assert.rejects(one("select public.grisilith_profile('Alice2')"), /name_change_rate_limit/);
  await assert.rejects(one('select * from grisilith_private.profiles'), /permission denied/);
  await assert.rejects(one('delete from grisilith_private.runs'), /permission denied/);
  await assert.rejects(begin(run1,'TEST_MAP'), /check constraint/);
  await assert.rejects(begin(run1,'ROAD_TO_HOME','HARD',{...build,weapons:[{id:'WPN_FAKE',level:1}]}), /invalid_weapon/);
  await assert.rejects(begin(run1,'ROAD_TO_HOME','HARD',{...build,items:[{id:'ITEM_BOOK_OF_EXPERTIZE',count:1}]}), /invalid_item/);
  assert.equal((await begin(run1)).receipt.eligible,true);
  assert.equal((await begin(run1)).receipt.run_id,run1); // idempotent begin
  await auth(b);
  await assert.rejects(one("select public.grisilith_profile('aLiCe')"), /display_name_taken/);
  await one("select public.grisilith_profile('Bob')");
  await assert.rejects(finish(run1), /run_not_found/);
  await assert.rejects(one('select public.grisilith_heartbeat($1)',[run1]), /run_not_found/);
  await assert.rejects(begin(run1), /run_not_found/);

  await ageRun(run1); await auth(a);
  await assert.rejects(finish(run1,{...result,normal_kills:-1}), /invalid_result_number/);
  await assert.rejects(finish(run1,{...result,mini_boss_kills:999999999}), /implausible_result/);
  await assert.rejects(finish(run1,{...result,final_boss_kills:1}), /implausible_result/);
  await assert.rejects(finish(run1,{...result,was_successful:true}), /implausible_result/);
  await assert.rejects(finish(run1,{...result,duration_seconds:100000}), /implausible_result/);
  assert.equal((await finish(run1,{...result,score:999999999})).receipt.score,2319);
  assert.equal((await finish(run1,{...result,normal_kills:999999})).receipt.score,2319);
  assert.equal((await one('select public.grisilith_heartbeat($1) as receipt',[run1])).receipt.eligible,false);

  await begin(run2,'ROAD_TO_HOME','NORMAL'); await ageRun(run2); await auth(a);
  assert.equal((await finish(run2,{...result,normal_kills:1000})).receipt.score,2375);
  const top = (await one("select public.grisilith_leaderboard('ROAD_TO_HOME') as rows")).rows;
  assert.equal(top.length,1); assert.equal(top[0].score,2375); assert.equal(top[0].display_name,'Alice');
  assert(!('player_id' in top[0]) && !('run_id' in top[0]) && !('final_build' in top[0]));
  await assert.rejects(one("select public.grisilith_leaderboard('TEST_MAP')"), /invalid_map/);

  await begin(run3); await owner();
  await db.query("update grisilith_private.runs set last_seen_at=now()-interval '31 seconds' where run_id=$1",[run3]);
  await auth(a);
  assert.equal((await one('select public.grisilith_heartbeat($1) as receipt',[run3])).receipt.eligible,false);

  // Legitimate item-spawned bonus mini-bosses must not be mistaken for impossible stage kills.
  await owner();
  await db.query("update grisilith_private.runs set started_at=now()-interval '10 minutes' where player_id=$1",[a]);
  await auth(a);
  const bonusRun='10000000-0000-4000-8000-000000000004';
  await begin(bonusRun); await ageRun(bonusRun); await auth(a);
  assert.equal((await finish(bonusRun,{...result,stage_reached:1,mini_boss_kills:4,
    final_build:{...build,weapons:[]}})).receipt.accepted,true); // Selling/removing the last weapon is legal.
  assert.equal((await finish(run3)).receipt.accepted,false);
  // New heartbeat cannot revive an ineligible run.
  assert.equal((await one('select public.grisilith_heartbeat($1) as receipt',[run3])).receipt.eligible,false);

  // Exact integer score rounding at all supported difficulty/stage boundaries.
  for (const [difficulty,maxStage,pct] of [['EASY',25,33],['NORMAL',35,50],['HARD',50,100]]) {
    for (const stage of [1,17,maxStage]) {
      const id = `20000000-0000-4000-8000-${String(pct*100+stage).padStart(12,'0')}`;
      await owner();
      await db.query("update grisilith_private.runs set started_at=now()-interval '10 minutes' where player_id=$1",[b]);
      await auth(b); await begin(id,'ROAD_TO_HOME',difficulty); await ageRun(id); await auth(b);
      const payload={...result,normal_kills:101,unique_kills:1,mini_boss_kills:0,stage_reached:stage};
      assert.equal((await finish(id,payload)).receipt.score,Math.floor((104*pct*(10+stage)+500)/1000));
    }
  }
  // All security-definer entry points pin search_path; no implicit public execute grants.
  await owner();
  const functions=(await db.query("select proname,proconfig,prosecdef from pg_proc p join pg_namespace n on n.oid=p.pronamespace where n.nspname='public' and proname like 'grisilith_%'")).rows;
  assert.equal(functions.length,6);
  assert(functions.every(f=>!f.prosecdef));
  assert(functions.every(f=>f.proconfig.some(c=>c.startsWith('search_path='))));
  await db.exec(migration('20261017175709_grisilith_identity_leaderboard.sql'));
  await db.exec(migration('20261017182121_grisilith_name_phrase_boundaries.sql'));
  await db.exec(migration('20261017183850_grisilith_whitelist_covered_spans.sql'));
  const policy=JSON.parse(readFileSync(new URL('../../assets/online/name-policy.json',import.meta.url),'utf8'));
  assert.deepEqual((await one('select grisilith_private.name_policy() as policy')).policy,policy);
  for(const [text,blocked] of [
    ['F U C K',true],['FU CK',true],['F.U.C.K',true],['FUC K',false],['KAS SESİ',false],
    ['PEACOCK',false],['PEACOCK FUCK',true],['PEACOCKFUCK',true],['OCEAN',false],
    ['DOCUMENT',false],['STAMINA',false],['NAZİK',false],['SİK',true],['SIK',true],
    ['SIQ',true],['S1K',true],['5H1T',false],['ŞHIT',false],['FUCK FUCK',true],
    ['THE HO',true],['HORSE',false],['HO',true],['K A S SESİ',false],['HELLO CINDER',false],
    ['DARKPHOENIX',false],['SHADOWHUNTER',false],['PEACOCKFUCK',true],['COCKTAILCOCK',true]
  ]) assert.equal((await one('select grisilith_private.name_blocked($1) as blocked',[text])).blocked,blocked,text);
  await auth(a);
  assert.equal((await one('select public.grisilith_profile() as p')).p.name_confirmed,true);
  assert.equal((await one("select public.grisilith_profile('şövalye 12') as p")).p.display_name,'ŞÖVALYE 12');
  await assert.rejects(one("select public.grisilith_profile('F.U.C.K')"),/blocked_display_name/);
  await assert.rejects(one("select public.grisilith_profile('---')"),/invalid_display_name/);
  await assert.rejects(one("select public.grisilith_profile('A😀B')"),/invalid_display_name/);
  await one("select public.grisilith_profile('ICE')");
  await auth(b);
  await assert.rejects(one("select public.grisilith_profile('ice')"),/display_name_taken/);
  assert.equal((await one("select public.grisilith_name_available('ice') as n")).n.available,false);
  await one("select public.grisilith_profile('İCE')");
  await auth(a); await one("select public.grisilith_profile('')");
  assert.equal((await one('select public.grisilith_profile() as p')).p.display_name,a.toUpperCase());
  await auth(b); await one("select public.grisilith_profile('ICE')"); // Released immediately.
  // Each individual run appears; tie rank ignores duration, display order uses duration.
  await owner();
  await db.exec("update grisilith_private.runs set score=5,duration_seconds=100 where status='finished'");
  await db.query('update grisilith_private.runs set score=11,duration_seconds=90 where run_id=$1',[run1]);
  await db.query('update grisilith_private.runs set score=10,duration_seconds=60 where run_id=$1',[run2]);
  await db.query('update grisilith_private.runs set score=10,duration_seconds=30 where run_id=$1',[bonusRun]);
  await auth(a);
  const board=(await one('select public.grisilith_leaderboard_all() as board')).board;
  assert.equal(board.top.length,10);
  assert.deepEqual(board.top.slice(0,4).map(x=>x.rank),[1,2,2,4]);
  assert.equal(board.top[1].duration_seconds,30);
  assert.equal(board.footer_kind,'next'); assert(board.footer);
  assert(board.top.filter(x=>x.is_self).length>1);
  assert(board.top.every(x=>!('player_id' in x)&&!('run_id' in x)&&!('summary' in x)));
  await one("select public.grisilith_profile('RENAMED')");
  assert((await one('select public.grisilith_leaderboard_all() as b')).b.top.filter(x=>x.is_self).every(x=>x.display_name==='RENAMED'));
  await owner();
  const c='00000000-0000-4000-8000-000000000003';
  await db.query('insert into auth.users values($1)',[c]); await auth(c);
  const first=(await one('select public.grisilith_profile() as p')).p;
  assert.equal(first.name_confirmed,false); assert.equal(first.display_name,c.toUpperCase());
  await one("select public.grisilith_profile('')");
  assert.equal((await one('select public.grisilith_profile() as p')).p.name_confirmed,true);
  assert.equal((await one('select public.grisilith_leaderboard_all() as b')).b.footer,null);
  const personalId='30000000-0000-4000-8000-000000000001';
  await begin(personalId,'TROUBLE_AT_HOME'); await ageRun(personalId); await auth(c);
  await finish(personalId,{...result,normal_kills:1,unique_kills:0,mini_boss_kills:0,stage_reached:1});
  const personalBoard=(await one('select public.grisilith_leaderboard_all() as b')).b;
  assert.equal(personalBoard.footer_kind,'personal');
  assert.equal(personalBoard.footer.score,1); assert.equal(personalBoard.footer.is_self,true);
  assert.equal(personalBoard.footer.rank,13);
  await db.exec('reset role; set role anon');
  await assert.rejects(one('select public.grisilith_leaderboard_all()'),/permission denied/);
  await assert.rejects(one("select grisilith_private.name_policy()"),/permission denied/);
  console.log('ONLINE_IDENTITY_LEADERBOARD_POSTGRES_SMOKE_PASS');
  console.log('ONLINE_DATABASE_POSTGRES_SMOKE_PASS');
} finally { await db.close(); }
