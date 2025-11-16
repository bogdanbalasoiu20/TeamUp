do $$
begin
if not exists(select 1 from pg_type where typname = 'user_role') then
   create TYPE user_role as ENUM('USER','ADMIN');
end if;
end $$;

alter table users
add column if not exists role user_role not null default 'USER';

update users
set role = 'USER' where role is null;