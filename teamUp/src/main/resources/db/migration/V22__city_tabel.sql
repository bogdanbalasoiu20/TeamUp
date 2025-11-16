
create extension if not exists unaccent;


create table if not exists city (
    id uuid primary key default gen_random_uuid(),
    name text not null,
    slug text not null unique,
    min_lat double precision,
    min_lng double precision,
    max_lat double precision,
    max_lng double precision,
    center_lat double precision,
    center_lng double precision,
    country_code char(5)
    );

create unique index if not exists uq_city_slug on city(slug);


create or replace function slugify(input text)
returns text language sql immutable as $$
select lower( regexp_replace( regexp_replace( unaccent(coalesce(input,'')), '[^a-zA-Z0-9\-_]+', '-', 'g'), '(^-|-$)', '', 'g') );
$$;

-- 4) Backfill: insereaza orasele DISTINCT din venues.city (daca exista coloana string)
insert into city (name, slug, country_code)
select name, slugify(name), 'RO'
from (
         select distinct trim(both ' ' from v.city) as name
         from venues v
         where v.city is not null and length(trim(v.city)) > 0
     ) s
where not exists (select 1 from city c where c.slug = slugify(s.name));

-- 5) Adauga FK la venues
alter table venues add column if not exists city_id uuid;
alter table venues
    add constraint fk_venue_city
        foreign key (city_id) references city(id);

create index if not exists ix_venues_city_id on venues(city_id);

-- 6) Populeaza venues.city_id din city.slug (match pe unaccent(name))
update venues v
set city_id = c.id
    from city c
where v.city_id is null
  and unaccent(upper(v.city)) = unaccent(upper(c.name));


