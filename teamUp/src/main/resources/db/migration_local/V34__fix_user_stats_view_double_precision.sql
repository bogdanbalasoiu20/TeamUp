
drop view if exists user_stats;

create view user_stats as
select
    rated_user_id as user_id,
    round(avg(pace)::numeric, 2)::double precision       as avg_pace,
    round(avg(shooting)::numeric, 2)::double precision   as avg_shooting,
    round(avg(passing)::numeric, 2)::double precision    as avg_passing,
    round(avg(defending)::numeric, 2)::double precision  as avg_defending,
    round(avg(dribbling)::numeric, 2)::double precision  as avg_dribbling,
    round(avg(physical)::numeric, 2)::double precision   as avg_physical,
    count(*)                                             as total_ratings
from player_ratings
group by rated_user_id;
