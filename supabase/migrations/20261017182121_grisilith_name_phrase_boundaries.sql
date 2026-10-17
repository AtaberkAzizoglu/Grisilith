begin;
create or replace function grisilith_private.name_blocked(v text) returns boolean
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
        -- Explicit multiword phrases must not accidentally bridge two longer benign words.
        if strpos(bad,' ')>0 and (
          (at_pos>1 and strpos('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ÇĞÖŞÜ',substr(candidate,at_pos-1,1))>0)
          or (at_pos+length(bad)<=length(candidate) and strpos('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ÇĞÖŞÜ',substr(candidate,at_pos+length(bad),1))>0)) then
          from_pos:=at_pos+1; continue;
        end if;
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


commit;

