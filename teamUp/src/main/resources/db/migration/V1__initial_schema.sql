CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;


--Enums
CREATE TYPE friend_request_status AS ENUM ('PENDING','ACCEPTED','DECLINED','CANCELED');
CREATE TYPE match_participant_status AS ENUM ('REQUESTED','ACCEPTED','DECLINED','LEFT','INVITED','KICKED','WAITLIST');
CREATE TYPE match_status AS ENUM ('OPEN','CANCELED','DONE','FULL');
CREATE TYPE match_visibility AS ENUM ('PUBLIC','PRIVATE','FRIENDS');
CREATE TYPE notification_type AS ENUM ('FRIEND_REQUEST','NEW_MATCH');
CREATE TYPE player_position_enum AS ENUM ('GOALKEEPER','DEFENDER','MIDFIELDER','FORWARD');
CREATE TYPE user_role AS ENUM ('ADMIN','USER');
CREATE TYPE venue_source AS ENUM ('OSM','USER','ADMIN','VENUE');


--Users
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       username VARCHAR(255) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       birthday DATE,
                       phone_number VARCHAR(255) NOT NULL,
                       player_position player_position_enum,
                       city VARCHAR(255),
                       description VARCHAR(300),
                       rank VARCHAR(255),
                       photo_url VARCHAR(255),
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                       password_changed_at TIMESTAMPTZ,
                       token_version INT NOT NULL DEFAULT 0,
                       is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                       role user_role NOT NULL DEFAULT 'USER'
);




--Cities
CREATE TABLE cities (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        name VARCHAR(120) NOT NULL,
                        slug VARCHAR(120) NOT NULL UNIQUE,
                        min_lat DOUBLE PRECISION,
                        min_lng DOUBLE PRECISION,
                        max_lat DOUBLE PRECISION,
                        max_lng DOUBLE PRECISION,
                        center_lat DOUBLE PRECISION,
                        center_lng DOUBLE PRECISION,
                        country_code VARCHAR(5)
);



--Venues
CREATE TABLE venues (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        name VARCHAR(255) NOT NULL,
                        address VARCHAR(255),
                        phone_number VARCHAR(255),
                        city_id UUID,
                        latitude DOUBLE PRECISION,
                        longitude DOUBLE PRECISION,

                        osm_type VARCHAR(16),
                        osm_id BIGINT,
                        tags_json JSONB,
                        source venue_source NOT NULL DEFAULT 'OSM',
                        is_active BOOLEAN NOT NULL DEFAULT TRUE,

                        geom geometry(Point,4326),
                        area_geom geometry(Geometry,4326),

                        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


--Matches
CREATE TABLE matches (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         creator_user_id UUID NOT NULL,
                         venue_id UUID NOT NULL,
                         starts_at TIMESTAMPTZ NOT NULL,
                         ends_at TIMESTAMPTZ,
                         duration_min INT,
                         max_players INT NOT NULL,
                         current_players INT NOT NULL DEFAULT 0,
                         join_deadline TIMESTAMPTZ,
                         title VARCHAR(255),
                         notes TEXT,
                         status match_status NOT NULL DEFAULT 'OPEN',
                         visibility match_visibility NOT NULL DEFAULT 'PUBLIC',
                         total_price NUMERIC(10,2),
                         created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                         updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                         is_active BOOLEAN NOT NULL DEFAULT TRUE,
                         version BIGINT NOT NULL DEFAULT 0
);


--Match_participants
CREATE TABLE match_participants (
                                    match_id UUID NOT NULL,
                                    user_id UUID NOT NULL,
                                    status match_participant_status NOT NULL DEFAULT 'REQUESTED',
                                    message TEXT,
                                    brings_ball BOOLEAN NOT NULL DEFAULT FALSE,
                                    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                    PRIMARY KEY (match_id, user_id)
);



--Friend_requests
CREATE TABLE friend_requests (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 requester_id UUID NOT NULL,
                                 addressee_id UUID NOT NULL,
                                 status friend_request_status NOT NULL DEFAULT 'PENDING',
                                 message TEXT,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 responded_at TIMESTAMPTZ,
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);



--Friendships
CREATE TABLE friendships (
                             user_a UUID NOT NULL,
                             user_b UUID NOT NULL,
                             created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                             PRIMARY KEY (user_a, user_b)
);



--Match_chat_messages
CREATE TABLE match_chat_messages (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     match_id UUID NOT NULL,
                                     sender_id UUID NOT NULL,
                                     content TEXT NOT NULL,
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


--notifications
CREATE TABLE notifications (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id UUID NOT NULL,
                               type notification_type NOT NULL,
                               title VARCHAR(255),
                               body TEXT,
                               payload JSONB,
                               is_seen BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                               seen_at TIMESTAMPTZ
);


--player_ratings
CREATE TABLE player_ratings (
                                match_id UUID NOT NULL,
                                rater_user_id UUID NOT NULL,
                                rated_user_id UUID NOT NULL,
                                pace SMALLINT NOT NULL,
                                shooting SMALLINT NOT NULL,
                                passing SMALLINT NOT NULL,
                                defending SMALLINT NOT NULL,
                                dribbling SMALLINT NOT NULL,
                                physical SMALLINT NOT NULL,
                                comment VARCHAR(500),
                                created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                PRIMARY KEY (match_id, rater_user_id, rated_user_id)
);


--foreign keys
ALTER TABLE venues
    ADD CONSTRAINT fk_venue_city FOREIGN KEY (city_id) REFERENCES cities(id);

ALTER TABLE matches
    ADD CONSTRAINT fk_match_creator FOREIGN KEY (creator_user_id) REFERENCES users(id);

ALTER TABLE matches
    ADD CONSTRAINT fk_match_venue FOREIGN KEY (venue_id) REFERENCES venues(id);

ALTER TABLE match_participants
    ADD CONSTRAINT fk_mp_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE;

ALTER TABLE match_participants
    ADD CONSTRAINT fk_mp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE friend_requests
    ADD CONSTRAINT fk_fr_requester FOREIGN KEY (requester_id) REFERENCES users(id);

ALTER TABLE friend_requests
    ADD CONSTRAINT fk_fr_addressee FOREIGN KEY (addressee_id) REFERENCES users(id);

ALTER TABLE friendships
    ADD CONSTRAINT fk_fs_a FOREIGN KEY (user_a) REFERENCES users(id);

ALTER TABLE friendships
    ADD CONSTRAINT fk_fs_b FOREIGN KEY (user_b) REFERENCES users(id);

ALTER TABLE match_chat_messages
    ADD CONSTRAINT fk_mcm_match FOREIGN KEY (match_id) REFERENCES matches(id);

ALTER TABLE match_chat_messages
    ADD CONSTRAINT fk_mcm_sender FOREIGN KEY (sender_id) REFERENCES users(id);

ALTER TABLE notifications
    ADD CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE player_ratings
    ADD CONSTRAINT fk_pr_match FOREIGN KEY (match_id) REFERENCES matches(id);

ALTER TABLE player_ratings
    ADD CONSTRAINT fk_pr_rater FOREIGN KEY (rater_user_id) REFERENCES users(id);

ALTER TABLE player_ratings
    ADD CONSTRAINT fk_pr_rated FOREIGN KEY (rated_user_id) REFERENCES users(id);



--indexes
CREATE INDEX ix_city_slug ON cities(slug);
CREATE UNIQUE INDEX ux_venues_osm ON venues(osm_type, osm_id)
    WHERE osm_type IS NOT NULL AND osm_id IS NOT NULL;

CREATE INDEX idx_venues_name_lower ON venues ((LOWER(name)));
CREATE INDEX idx_venues_active ON venues(is_active);
CREATE INDEX idx_venues_tags_gin ON venues USING GIN (tags_json);

CREATE INDEX idx_venues_name_trgm ON venues USING gin (name gin_trgm_ops);


CREATE INDEX idx_venues_geom ON venues USING GIST (geom);
CREATE INDEX idx_venues_area_geom ON venues USING GIST (area_geom);
CREATE INDEX idx_venues_geom_geog ON venues USING GIST ((geom::geography));
CREATE INDEX idx_venues_geom_active ON venues USING GIST (geom) WHERE is_active = true;

CREATE INDEX idx_venues_coalesced_geom_gist ON venues USING GIST (coalesce(area_geom, geom));



--triggers
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger AS $$
BEGIN
    NEW.updated_at = now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_venues_set_updated_at
    BEFORE UPDATE ON venues
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();



CREATE OR REPLACE FUNCTION venues_sync_geom()
RETURNS trigger AS $$
BEGIN
    IF NEW.latitude IS NOT NULL AND NEW.longitude IS NOT NULL THEN
        NEW.geom := ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude),4326);
ELSE
        NEW.geom := NULL;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_venues_sync_geom
    BEFORE INSERT OR UPDATE ON venues
                         FOR EACH ROW
                         EXECUTE FUNCTION venues_sync_geom();
