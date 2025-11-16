DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'venue_source') THEN
CREATE TYPE venue_source AS ENUM ('OSM', 'USER', 'OWNER', 'ADMIN');
END IF;
END $$;

ALTER TABLE venues
    ADD COLUMN IF NOT EXISTS osm_type   VARCHAR(16),
    ADD COLUMN IF NOT EXISTS osm_id     BIGINT,
    ADD COLUMN IF NOT EXISTS tags_json  JSONB,
    ADD COLUMN IF NOT EXISTS source     venue_source,
    ADD COLUMN IF NOT EXISTS is_active  BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE venues ALTER COLUMN source SET DEFAULT 'OSM';
UPDATE venues SET source = 'OSM' WHERE source IS NULL;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_indexes
    WHERE schemaname = 'public' AND indexname = 'ux_venues_osm'
  ) THEN
CREATE UNIQUE INDEX ux_venues_osm ON venues(osm_type, osm_id)
    WHERE osm_type IS NOT NULL AND osm_id IS NOT NULL;
END IF;
END $$;


DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_indexes
    WHERE schemaname = 'public' AND indexname = 'idx_venues_name_lower'
  ) THEN
CREATE INDEX idx_venues_name_lower ON venues (LOWER(name));
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_indexes
    WHERE schemaname = 'public' AND indexname = 'idx_venues_city_lower'
  ) THEN
CREATE INDEX idx_venues_city_lower ON venues (LOWER(city));
END IF;
END $$;