create table player_ratings (
    match_id uuid not null,
    rater_user_id uuid not null,
    rated_user_id uuid not null,

    pace smallint not null check (pace between 1 and 10),
    shooting smallint not null check (shooting between 1 and 10),
    passing smallint not null check (passing between 1 and 10),
    dribbling smallint not null check (dribbling between 1 and 10),
    defending smallint not null check (defending between 1 and 10),
    physical smallint not null check (physical between 1 and 10),

    comment text,
    created_at timestamptz not null default now(),

    primary key (match_id, rater_user_id, rated_user_id),

    constraint fk_pr_match foreign key (match_id)
        references matches(id) on delete cascade,

    constraint fk_pr_rater foreign key (rater_user_id)
        references users(id) on delete cascade,

    constraint fk_pr_rated foreign key (rated_user_id)
        references users(id) on delete cascade,

    constraint chk_pr_not_self check (rater_user_id <> rated_user_id)
);

create index idx_pr_rated_user on player_ratings (rated_user_id);
create index idx_pr_match on player_ratings (match_id);



create or replace view user_stats as
select
    rated_user_id as user_id,
    round(avg(pace), 2)       as avg_pace,
    round(avg(shooting), 2)   as avg_shooting,
    round(avg(passing), 2)    as avg_passing,
    round(avg(defending), 2)  as avg_defending,
    round(avg(dribbling), 2)  as avg_dribbling,
    round(avg(physical), 2)   as avg_physical,
    count(*)                  as total_ratings
from player_ratings
group by rated_user_id;
