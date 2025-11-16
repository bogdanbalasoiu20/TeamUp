alter table cities
add column if not exists area_geom geometry(MultiPolygon,4326);

create index if not exists idx_cities_area_geom on cities using GIST (area_geom);