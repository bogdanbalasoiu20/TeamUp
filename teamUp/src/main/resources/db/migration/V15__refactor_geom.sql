create index if not exists idx_venues_geom_geog on venues using gist ((geom::geography));

create index if not exists idx_venues_geom_active on venues using gist (geom) where is_active = true;