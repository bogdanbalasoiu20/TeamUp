ALTER TABLE cities
    ADD COLUMN area_geom geometry(Geometry, 4326),
    ADD COLUMN geom geometry(Point, 4326);
