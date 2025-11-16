create extension if not exists postgis;  --activez postgis(incarca functiile si tipurile spatiale) in bd

--adaug coloana geom; Point -> un punct (x,y); 4326 -> SRID WGS84(lat/long pe glob) - acelasi sistem de coordonate pe care il folosesc OSM/Leaflet/Google Maps
alter table venues
add column if not exists geom geometry(Point,4326);

--populeaza randurile de venues deja existente care au latitude si longitude, umple coloana geom cu punctul corespunzator
--ST_SetSRID ataseaza sistemul de coordonate WGS84 la geometrie
update venues
set geom = ST_SetSRID(ST_MakePoint(longitude,latitude),4326)
where latitude is not null and longitude is not null;


-- folosesc un trigger cu Before pentru a valida randurile in bd.
--la fiecare insert sau update pe venues, daca lat si long sunt setate, calculez din nou geom
--daca oricare dintre coordonate este null, pun geom = null pentru a evita geometrii invalide
create or replace function venues_sync_geom() returns trigger as $$
begin
if new.latitude is not null and new.longitude is not null then
   new.geom := ST_SetSRID(ST_MakePoint(new.longitude, new.latitude),4326);
else
   new.geom := null;
end if;
return new;
end $$ language plpgsql;

drop trigger if exists trg_venues_sync_geom on venues;
create trigger trg_venues_sync_geom
before insert or update on venues
for each row execute function venues_sync_geom();

create index if not exists idx_venues_geom on venues using GIST (geom);