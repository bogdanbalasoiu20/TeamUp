create table test_table(
    id serial primary key,
    name varchar(100) not null,
    created_at TIMESTAMP default NOW()
);