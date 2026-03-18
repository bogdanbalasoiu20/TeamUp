alter table tournament_matches
add column if not exists odds_home double precision,
add column if not exists odds_away double precision,
add column if not exists odds_draw double precision;
