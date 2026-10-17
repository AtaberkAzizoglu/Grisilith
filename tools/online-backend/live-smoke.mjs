// Explicit live test. Creates one temporary anonymous QA user only after verifying API version.
// Tokens stay in process memory, are never printed, and are signed out in finally.
// The caller must remove the returned QA user ID through the management connection afterward.
import assert from 'node:assert/strict';
import { randomUUID } from 'node:crypto';
if (!process.argv.includes('--live')) throw new Error('Explicit --live opt-in required');
const url='https://wadrsdretczaoarjduko.supabase.co';
const key='sb_publishable_9KkX_OJA88ez_UdJ1WGoqg_9i6CjbkF';
let token, userId, runId, phase='health', logoutSucceeded=false;
async function request(path,body={},authenticated=true) {
  const response=await fetch(url+path,{
    method:'POST',headers:{apikey:key,'Content-Type':'application/json',...(authenticated&&token?{Authorization:`Bearer ${token}`}:{})},
    body:JSON.stringify(body),signal:AbortSignal.timeout(12000),redirect:'error'
  });
  const text=await response.text();
  const data=text ? JSON.parse(text) : null;
  if(!response.ok) {
    const error=new Error('Live request failed');
    error.status=response.status;
    error.code=data?.error_code||data?.code||'unknown';
    throw error;
  }
  return data;
}
const build={weapons:[{id:'WPN_LONGSWORD',level:1}],armor:'ARMOR_LEATHER',items:[],tamgas:[],stats:{STRENGTH:12}};
let report;
try {
  const health=await request('/rest/v1/rpc/grisilith_api_status',{},false);
  assert.equal(health.api_version,1);
  phase='anonymous_auth';
  const session=await request('/auth/v1/signup',{data:{grisilith_qa:true,purpose:'phase8b_connection_test',nonce:randomUUID()}},false);
  token=session.access_token;userId=session.user.id;
  assert(token&&userId);
  phase='profile';
  const profile=await request('/rest/v1/rpc/grisilith_profile');
  assert.equal(profile.player_id,userId);assert.equal(profile.display_name,userId.toUpperCase());
  assert.equal(profile.name_confirmed,false);
  const name='QA_'+randomUUID().replaceAll('-','').slice(0,14).toUpperCase();
  const renamed=await request('/rest/v1/rpc/grisilith_profile',{p_display_name:name});
  assert.equal(renamed.display_name,name);assert.equal(renamed.name_confirmed,true);
  assert.equal((await request('/rest/v1/rpc/grisilith_name_available',{p_name:name})).available,true);
  await request('/rest/v1/rpc/grisilith_profile',{p_display_name:''});
  assert.equal((await request('/rest/v1/rpc/grisilith_profile')).name_confirmed,true);
  phase='begin';runId=randomUUID();
  const begin=await request('/rest/v1/rpc/grisilith_begin_run',{
    p_run_id:runId,p_map_id:'ROAD_TO_HOME',p_difficulty:'HARD',p_character_id:'CHR_SIR_IMBERT',
    p_game_version:'0.3',p_starting_build:build
  });
  assert.equal(begin.eligible,true);
  phase='heartbeat';
  assert.equal((await request('/rest/v1/rpc/grisilith_heartbeat',{p_run_id:runId})).eligible,true);
  phase='finish';
  const result={normal_kills:10,unique_kills:0,mini_boss_kills:0,final_boss_kills:0,stage_reached:1,
    duration_seconds:0,was_successful:false,final_build:build,summary:{qa:true}};
  const receipt=await request('/rest/v1/rpc/grisilith_finish_run',{p_run_id:runId,p_result:result});
  assert.equal(receipt.accepted,true);assert.equal(receipt.score,11);
  phase='duplicate_submission';
  const duplicate=await request('/rest/v1/rpc/grisilith_finish_run',{
    p_run_id:runId,p_result:{...result,normal_kills:99999999}
  });
  assert.equal(duplicate.score,11);
  phase='leaderboard';
  const board=await request('/rest/v1/rpc/grisilith_leaderboard_all');
  const rows=board.top;
  assert(Array.isArray(rows)&&rows.length<=10);
  assert(!rows.some(r=>r.player_id===userId));
  report={pass:true,checks:['https','anonymous_auth','profile','begin','heartbeat','server_score','idempotency','leaderboard'],qa_user_id:userId,qa_run_id:runId};
} catch(error) {
  report={pass:false,phase,status:error.status??null,code:error.code??error.name,qa_user_id:userId??null,qa_run_id:runId??null};
  process.exitCode=1;
} finally {
  if(token) {
    try{ await request('/auth/v1/logout?scope=global');logoutSucceeded=true; }catch{}
  }
  if(report) console.log(JSON.stringify({...report,logout_succeeded:logoutSucceeded}));
}
