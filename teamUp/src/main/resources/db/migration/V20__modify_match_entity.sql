do $$
begin
    if not exists (select 1 from pg_type where typname = 'match_status') then
        create type match_status as enum('OPEN','CANCELED','DONE','FULL');
    end if;
    if not exists (select 1 from pg_type where typname = 'match_visibility') then
        create type match_visibility as enum ('PUBLIC', 'PRIVATE', 'FRIENDS');
    end if;
end $$;

alter table matches
add column if not exists visibility match_visibility default 'PUBLIC';

update matches
set visibility = 'PUBLIC' where visibility is null;

alter table matches
alter column visibility set not null;

alter table matches
alter column status drop default ,
alter column status type match_status using status::match_status,
alter column status set default 'OPEN',
alter column status set not null;

alter table matches drop constraint if exists chk_match_status;

create index if not exists idx_matches_status on matches(status);
create index if not exists idx_matches_visibility on matches(visibility);
create index if not exists idx_matches_creator on matches(creator_user_id);


create or replace function set_updated_at_matches() returns trigger as $$
begin new.updated_at := now();
return new;
end $$ language plpgsql;

drop trigger if exists trg_matches_set_updated_at on matches;
create trigger trg_matches_set_updated_at
before update on matches
for each row execute function set_updated_at_matches();


