alter table matches
    add column if not exists ends_at timestamptz;

update matches
set ends_at = starts_at + (coalesce(duration_min, 0) * interval '1 minute')
where ends_at is null;

create or replace function matches_set_ends_at() returns trigger as $$
begin
  new.ends_at := new.starts_at + (coalesce(new.duration_min, 0) * interval '1 minute');
return new;
end $$ language plpgsql;

drop trigger if exists trg_matches_set_ends_at on matches;
create trigger trg_matches_set_ends_at
    before insert or update of starts_at, duration_min on matches
    for each row execute function matches_set_ends_at();

alter table matches
    alter column ends_at set not null;

alter table matches
    add column if not exists current_players int not null default 0;

alter table matches
    add column if not exists join_deadline timestamptz;

alter table matches
    add column if not exists is_active boolean not null default true;

alter table matches
    add column if not exists version bigint not null default 0;

do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conname = 'chk_players_bounds'
      and conrelid = 'matches'::regclass
  ) then
alter table matches
    add constraint chk_players_bounds
        check (current_players >= 0 and current_players <= max_players);
end if;
end $$;

create index if not exists ix_matches_active_future on matches(is_active, starts_at);
create index if not exists ix_matches_deadline on matches(join_deadline);
create index if not exists ix_matches_end_at on matches(ends_at);
