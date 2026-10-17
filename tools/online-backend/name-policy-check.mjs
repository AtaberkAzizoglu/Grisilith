// Read-only consistency check. Policy changes require a NEW migration, never editing a deployed one.
import {readFileSync,readdirSync} from 'node:fs';
import assert from 'node:assert/strict';
const policy=JSON.parse(readFileSync('assets/online/name-policy.json','utf8'));
const files=readdirSync('supabase/migrations').filter(x=>x.endsWith('.sql')).sort().reverse();
const migration=files.map(x=>readFileSync(`supabase/migrations/${x}`,'utf8')).find(x=>x.includes('function grisilith_private.name_policy()'));
assert(migration.includes(JSON.stringify(policy).replaceAll("'","''")),'Server/client policy data differ');
console.log(`NAME_POLICY_SYNC_PASS version=${policy.version} banned=${policy.banned.length} whitelist=${policy.whitelist.length}`);
