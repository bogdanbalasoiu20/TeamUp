create table users(
    id uuid primary key default gen_random_uuid(),
    username TEXT not null,
    email TEXT not null,
    password_hash TEXT not null,
    birthday date,
    phone_number TEXT not null,
    position TEXT,
    city TEXT,
    description TEXT,
    rank TEXT,
    photo_url TEXT,
    created_at TIMESTAMPTZ not null default now(),
    updated_at TIMESTAMPTZ not null default now()
);

create unique index uq_users_email_ci on users(lower(email));
create unique index uq_users_username_ci on users(lower(username));


create table skills(
    code TEXT primary key,
    name TEXT not null,
    description TEXT,
    sort_order INT,
    is_active BOOLEAN not null default true
);

create table skill_endorsements(
    target_user_id uuid not null,
    voter_user_id uuid not null,
    skill_code text not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    primary key (target_user_id,voter_user_id,skill_code),
    constraint fk_se_target foreign key (target_user_id) references users(id) on delete cascade,
    constraint fk_se_voter foreign key (voter_user_id) references users(id) on delete cascade,
    constraint fk_se_skill foreign key (skill_code) references skills(code) on delete restrict ,
    constraint chk_se_not_self check (target_user_id <> voter_user_id)
);

create index idx_se_target_skill on skill_endorsements(target_user_id, skill_code);


create table venues(
    id uuid primary key default gen_random_uuid(),
    name text not null,
    address text,
    phone_number text,
    city text,
    latitude double precision,
    longitude double precision,
    place_id text,
    is_active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint chk_location check(
        (latitude is null or (latitude between -90 and 90)) and
        (longitude is null or (longitude between -180 and 180))
        )
);

CREATE UNIQUE INDEX uq_venues_place_id ON venues(place_id) WHERE place_id IS NOT NULL;

create table matches(
    id uuid primary key default gen_random_uuid(),
    creator_user_id uuid not null,
    venue_id uuid not null,
    starts_at timestamptz not null,
    duration_min int,
    max_players int not null,
    title text,
    notes text,
    status varchar(20) not null default 'OPEN',
    total_price numeric(10,2),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint fk_match_creator foreign key (creator_user_id) references users(id) on delete restrict,
    constraint fk_match_venue foreign key (venue_id) references venues(id) on delete restrict,
    constraint chk_match_players check(max_players>0),
    constraint chk_match_dur check (duration_min is NULL or duration_min >0),
    constraint chk_match_status check (status in ('OPEN','CANCELED','DONE','FULL'))
);

create index idx_match_starts_at on matches(starts_at);
create index idx_matches_venue on matches(venue_id);


create table match_participants(
    match_id uuid not null,
    user_id uuid not null,
    status varchar(20) not null default 'REQUESTED',
    message text,
    brings_ball boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    primary key (match_id, user_id),
    constraint fk_mp_match foreign key (match_id) references matches(id) on delete cascade,
    constraint fk_mp_user foreign key (user_id) references users(id) on delete cascade,
    constraint chk_mp_status check (status in ('REQUESTED','ACCEPTED','DECLINED','LEFT'))
);

create index idx_mp_match_status on match_participants(match_id,status);
create index idx_mp_user_status on match_participants(user_id,status);


create table match_chat_messages(
    id uuid primary key default gen_random_uuid(),
    match_id uuid not null,
    sender_id uuid not null,
    content text not null check (length(trim(content))>0),
    created_at timestamptz not null default now(),

    constraint fk_mcm_match foreign key (match_id) references matches(id) on delete cascade,
    constraint fk_mcm_sender_is_participant foreign key (match_id,sender_id) references match_participants(match_id,user_id) on delete restrict
);

CREATE INDEX idx_mcm_match_created ON match_chat_messages(match_id, created_at DESC);
CREATE INDEX idx_mcm_match_sender  ON match_chat_messages(match_id, sender_id);



CREATE TABLE notifications (
   id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
   user_id    uuid        NOT NULL,
   type       TEXT        NOT NULL,
   title      TEXT,
   body       TEXT,
   payload    JSONB,
   is_seen    BOOLEAN     NOT NULL DEFAULT FALSE,
   created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
   seen_at    TIMESTAMPTZ,

   CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notif_user_seen ON notifications(user_id, is_seen, created_at DESC);



-- Cereri de prietenie
CREATE TABLE friend_requests (
                                 id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                 requester_id  uuid        NOT NULL,  -- cine trimite
                                 addressee_id  uuid        NOT NULL,  -- cine primește
                                 status        TEXT        NOT NULL DEFAULT 'PENDING',
                                 message       TEXT,
                                 created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 responded_at  TIMESTAMPTZ,
                                 updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),

                                 CONSTRAINT fk_fr_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_fr_addressee FOREIGN KEY (addressee_id) REFERENCES users(id) ON DELETE CASCADE,

    -- interzice sa-ti trimiți singur cerere
                                 CONSTRAINT chk_fr_not_self CHECK (requester_id <> addressee_id),

    -- status permis
                                 CONSTRAINT chk_fr_status CHECK (status IN ('PENDING','ACCEPTED','DECLINED','CANCELED'))
);

-- un singur PENDING între doi useri, indiferent de direcție
CREATE UNIQUE INDEX uq_fr_pair_pending
    ON friend_requests (LEAST(requester_id, addressee_id), GREATEST(requester_id, addressee_id))
    WHERE status = 'PENDING';

-- cel mult o relație ACCEPTED între doi useri, indiferent de direcție
CREATE UNIQUE INDEX uq_fr_pair_accepted
    ON friend_requests (LEAST(requester_id, addressee_id), GREATEST(requester_id, addressee_id))
    WHERE status = 'ACCEPTED';

-- listări rapide
CREATE INDEX idx_fr_incoming_pending ON friend_requests(addressee_id)
    WHERE status = 'PENDING';
CREATE INDEX idx_fr_outgoing_pending ON friend_requests(requester_id)
    WHERE status = 'PENDING';
CREATE INDEX idx_fr_user_any ON friend_requests(LEAST(requester_id, addressee_id), GREATEST(requester_id, addressee_id));



CREATE TABLE friendships (
     user_a    uuid NOT NULL,
     user_b    uuid NOT NULL,
     created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
     PRIMARY KEY (user_a, user_b),
     CONSTRAINT fk_fs_a FOREIGN KEY (user_a) REFERENCES users(id) ON DELETE CASCADE,
     CONSTRAINT fk_fs_b FOREIGN KEY (user_b) REFERENCES users(id) ON DELETE CASCADE,
     CONSTRAINT chk_fs_order CHECK (user_a < user_b)  -- normalizează perechea
);

CREATE INDEX idx_fs_user_b ON friendships(user_b);

