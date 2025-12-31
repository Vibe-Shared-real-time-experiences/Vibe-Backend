-- ==========================================
-- 1. IDENTITY & SOCIAL GRAPH
-- ==========================================

-- The Global User
CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50) NOT NULL,
    password   TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    is_active  BOOLEAN                  DEFAULT TRUE,
    is_deleted BOOLEAN                  DEFAULT FALSE
);

CREATE TABLE user_profiles
(
    user_id      BIGSERIAL PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    display_name VARCHAR(100),
    avatar_url   TEXT,
    bio          TEXT,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_public    BOOLEAN                  DEFAULT TRUE,
    is_deleted   BOOLEAN                  DEFAULT FALSE
);

CREATE TABLE roles
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) UNIQUE NOT NULL,
    description varchar(255),
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE,
    is_active   BOOLEAN   DEFAULT TRUE,
    is_deleted  BOOLEAN   DEFAULT FALSE
);

CREATE TABLE permissions
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) UNIQUE NOT NULL, -- e.g. "VIEW_CHANNEL", "BAN_MEMBERS"
    description TEXT,
    code        INT                 NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE,
    is_active   BOOLEAN   DEFAULT TRUE,
    is_deleted  BOOLEAN   DEFAULT FALSE
);


CREATE TABLE user_roles
(
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGSERIAL NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id BIGSERIAL NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    UNIQUE (user_id, role_id)
);

CREATE TABLE role_permissions
(
    id            BIGSERIAL PRIMARY KEY,
    role_id       BIGSERIAL NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id BIGSERIAL       NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    UNIQUE (role_id, permission_id)
);

CREATE TABLE relationships
(
    id         BIGSERIAL PRIMARY KEY,
    user_id_1  BIGSERIAL NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    user_id_2  BIGSERIAL NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status     VARCHAR(20) CHECK (status IN ('PENDING', 'FRIEND', 'BLOCKED')),
    UNIQUE (user_id_1, user_id_2)
);

CREATE TABLE servers
(
    id          BIGSERIAL PRIMARY KEY,
    owner_id    BIGSERIAL    NOT NULL REFERENCES users (id),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    icon_url    TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE,
    is_public   BOOLEAN                  DEFAULT TRUE,
    is_active   BOOLEAN                  DEFAULT TRUE,
    is_deleted  BOOLEAN                  DEFAULT FALSE
);

CREATE TABLE categories
(
    id         BIGSERIAL PRIMARY KEY,
    server_id  BIGSERIAL   NOT NULL REFERENCES servers (id) ON DELETE CASCADE,
    name       VARCHAR(50) NOT NULL,
    position   INT                      DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    is_public  BOOLEAN                  DEFAULT TRUE,
    is_active  BOOLEAN                  DEFAULT TRUE,
    is_deleted BOOLEAN                  DEFAULT FALSE
);

CREATE TABLE channels
(
    id          BIGSERIAL PRIMARY KEY,
    server_id   BIGSERIAL REFERENCES servers (id) ON DELETE CASCADE,
    category_id BIGSERIAL   REFERENCES categories (id) ON DELETE SET NULL,
    name        VARCHAR(100), -- Nullable for DMs
    type        VARCHAR(20) NOT NULL CHECK (type IN ('TEXT', 'VOICE', 'DM')),
    position    INT                      DEFAULT 0,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE,
    is_deleted  BOOLEAN                  DEFAULT FALSE
);

CREATE TABLE server_members
(
    id        BIGSERIAL PRIMARY KEY,
    server_id BIGSERIAL NOT NULL REFERENCES servers (id) ON DELETE CASCADE,
    user_id   BIGSERIAL NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    nickname  VARCHAR(32),
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_active BOOLEAN                  DEFAULT TRUE,
    UNIQUE (server_id, user_id)
);

CREATE TABLE member_roles
(
    id         BIGSERIAL PRIMARY KEY,
    server_id  BIGSERIAL   NOT NULL REFERENCES servers (id) ON DELETE CASCADE,
    name       VARCHAR(50) NOT NULL,
    color      VARCHAR(7),
    position   INT       DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    is_active  BOOLEAN   DEFAULT TRUE,
    is_deleted BOOLEAN   DEFAULT FALSE
);

CREATE TABLE channel_messages
(
    id             BIGINT PRIMARY KEY,
    channel_id     BIGSERIAL NOT NULL REFERENCES channels (id) ON DELETE CASCADE,
    author_id      BIGSERIAL NOT NULL REFERENCES users (id),
    content        TEXT,
    -- List of jsonb objects with attachment info
    attachments    JSONB,
    meta_data      JSONB, -- For reactions, embeds, etc.
    is_pinned      BOOLEAN                  DEFAULT FALSE,
    is_edited      BOOLEAN                  DEFAULT FALSE,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE,
    is_deleted     BOOLEAN                  DEFAULT FALSE
);

CREATE TABLE conversations
(
    id         BIGSERIAL PRIMARY KEY,
    is_group   BOOLEAN                  DEFAULT FALSE,
    name       VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    is_active  BOOLEAN                  DEFAULT TRUE,
    is_deleted BOOLEAN                  DEFAULT FALSE
);

CREATE TABLE conversation_participants
(
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGSERIAL NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    user_id         BIGSERIAL NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    joined_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE (conversation_id, user_id)
);

CREATE TABLE conversation_messages
(
    id              BIGINT PRIMARY KEY,
    conversation_id BIGSERIAL NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    author_id       BIGSERIAL NOT NULL REFERENCES users (id),
    content         TEXT,
    attachments    JSONB,
    meta_data      JSONB, -- For reactions, embeds, etc.
    is_pinned       BOOLEAN                  DEFAULT FALSE,
    is_edited       BOOLEAN                  DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE,
    is_deleted      BOOLEAN                  DEFAULT FALSE
);

-- CREATE TABLE user_bios
-- (
--     id         BIGSERIAL PRIMARY KEY,
--     user_id    BIGSERIAL REFERENCES users (id) ON DELETE CASCADE,
--     bio_text   TEXT,
--     created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
--     updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
--     UNIQUE (user_id)
-- );
--

-- CREATE TABLE read_states
-- (
--     user_id              BIGINT NOT NULL REFERENCES users (id),
--     channel_id           BIGINT NOT NULL REFERENCES channels (id),
--     last_read_message_id BIGINT NOT NULL,
--     mention_count        INT       DEFAULT 0,
--     last_updated         TIMESTAMP DEFAULT NOW(),
--     PRIMARY KEY (user_id, channel_id)
-- );
--
-- CREATE TABLE mentions
-- (
--     id         BIGSERIAL PRIMARY KEY,
--     user_id    BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
--     message_id BIGINT NOT NULL REFERENCES messages (id) ON DELETE CASCADE,
--     server_id  BIGINT REFERENCES servers (id),
--     is_read    BOOLEAN                  DEFAULT FALSE,
--     created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
-- );