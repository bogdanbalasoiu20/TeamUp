-- flyway:executeInTransaction=false
create index concurrently if not exists idx_venues_coalesced_geom_gist on venues using gist (coalesce(area_geom,geom));