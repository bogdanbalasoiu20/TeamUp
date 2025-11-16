alter table venues
add column if not exists area_geom geometry(Geometry,4326);

create index if not exists idx_venues_area_geom on venues using GIST (area_geom);