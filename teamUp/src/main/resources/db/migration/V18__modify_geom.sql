update venues
set area_geom = ST_MakeValid(area_geom)
where area_geom is not null and not ST_IsValid(area_geom);

update venues
set geom = ST_Centroid(area_geom)
where geom is null and area_geom is not null;