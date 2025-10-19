create extension if not exists pg_trgm; --extensie care descompune textul in trigrame(ma ajuta la recomandari atunci cand caut un teren -> se afiseaza rezultatele ce contin ceea ce am introdus)

--gin imi ofera cautari foarte rapide -> creez indexuri pe name si city cu ilike/similirity
create index if not exists idx_venues_name_trgm on venues using gin (name gin_trgm_ops);
create index if not exists idx_venues_city_trgm on venues using gin (city gin_trgm_ops);