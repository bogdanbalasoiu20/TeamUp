ALTER TABLE venues
    ADD CONSTRAINT chk_osm_pair
        CHECK (
            (osm_type IS NULL AND osm_id IS NULL)
                OR (osm_type IS NOT NULL AND osm_id IS NOT NULL)
            );

CREATE INDEX IF NOT EXISTS idx_venues_tags_gin ON venues USING GIN (tags_json);

CREATE INDEX IF NOT EXISTS idx_venues_active ON venues(is_active);

ALTER TABLE venues ALTER COLUMN source SET NOT NULL;

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger AS $$
BEGIN
  NEW.updated_at := now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_venues_set_updated_at ON venues;
CREATE TRIGGER trg_venues_set_updated_at
    BEFORE UPDATE ON venues
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();