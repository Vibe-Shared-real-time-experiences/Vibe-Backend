-- ==========================================
-- 1. IDENTITY & SOCIAL GRAPH
-- ==========================================

-- The Global User
CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(255) NOT NULL,
    password   TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    is_active  BOOLEAN                  DEFAULT TRUE,
    is_deleted BOOLEAN                  DEFAULT FALSE
);

CREATE TABLE user_profiles
(
    user_id       BIGSERIAL PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    display_name  VARCHAR(100),
    date_of_birth DATE NOT NULL,
    avatar_url    TEXT,
    bio           TEXT,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_public     BOOLEAN                  DEFAULT TRUE,
    is_deleted    BOOLEAN                  DEFAULT FALSE
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
    name        VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
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
    permission_id BIGSERIAL NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    UNIQUE (role_id, permission_id)
);

CREATE TABLE relationships
(
    id        BIGSERIAL PRIMARY KEY,
    user_id_1 BIGSERIAL NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    user_id_2 BIGSERIAL NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status    VARCHAR(20) CHECK (status IN ('PENDING', 'FRIEND', 'BLOCKED')),
    UNIQUE (user_id_1, user_id_2)
);

-- ==========================================
-- 2. SERVERS, CHANNELS & MESSAGING
-- ==========================================
CREATE TABLE servers
(
    id          BIGSERIAL PRIMARY KEY,
    owner_id    BIGSERIAL    NOT NULL REFERENCES users (id),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    icon_url    TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE,
    is_public   BOOLEAN      NOT NULL    DEFAULT TRUE,
    is_active   BOOLEAN      NOT NULL    DEFAULT TRUE,
    is_deleted  BOOLEAN      NOT NULL    DEFAULT FALSE
);

CREATE TABLE categories
(
    id         BIGSERIAL PRIMARY KEY,
    server_id  BIGSERIAL   NOT NULL REFERENCES servers (id) ON DELETE CASCADE,
    name       VARCHAR(50) NOT NULL,
    position   INT         NOT NULL     DEFAULT 0,
    is_public  BOOLEAN     NOT NULL     DEFAULT TRUE,
    is_active  BOOLEAN     NOT NULL     DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    is_deleted BOOLEAN                  DEFAULT FALSE
);

CREATE TABLE channels
(
    id          BIGSERIAL PRIMARY KEY,
    server_id   BIGSERIAL REFERENCES servers (id) ON DELETE CASCADE,
    category_id BIGSERIAL REFERENCES categories (id) ON DELETE CASCADE,
    name        VARCHAR(100), -- Nullable for DMs
    type        VARCHAR(20) NOT NULL CHECK (type IN ('TEXT', 'VOICE', 'DM')),
    position    INT         NOT NULL     DEFAULT 0,
    is_public   BOOLEAN     NOT NULL     DEFAULT TRUE,
    is_active   BOOLEAN     NOT NULL     DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE,
    is_deleted  BOOLEAN     NOT NULL     DEFAULT FALSE
);

CREATE TABLE server_permissions
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) UNIQUE NOT NULL, -- e.g. "VIEW_CHANNEL", "BAN_MEMBERS"
    description TEXT,
    bitmask     BIGINT              NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE,
    is_active   BOOLEAN   DEFAULT TRUE,
    is_deleted  BOOLEAN   DEFAULT FALSE
);


CREATE TABLE server_members
(
    id         BIGSERIAL PRIMARY KEY,
    server_id  BIGSERIAL NOT NULL REFERENCES servers (id) ON DELETE CASCADE,
    user_id    BIGSERIAL NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    nickname   VARCHAR(100),
    joined_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_active  BOOLEAN   NOT NULL       DEFAULT TRUE,
    UNIQUE (server_id, user_id)
);

CREATE TABLE server_roles
(
    id                 BIGSERIAL PRIMARY KEY,
    server_id          BIGSERIAL   NOT NULL REFERENCES servers (id) ON DELETE CASCADE,
    name               VARCHAR(50) NOT NULL,
    color              VARCHAR(7),
    position           INT       DEFAULT 0,
    permission_bitmask BIGINT    DEFAULT 0,
    created_at         TIMESTAMP DEFAULT NOW(),
    updated_at         TIMESTAMP WITH TIME ZONE,
    is_active          BOOLEAN   DEFAULT TRUE,
    is_deleted         BOOLEAN   DEFAULT FALSE
);

CREATE TABLE server_has_permissions
(
    id            BIGSERIAL PRIMARY KEY,
    server_id     BIGSERIAL NOT NULL REFERENCES servers (id) ON DELETE CASCADE,
    permission_id BIGSERIAL NOT NULL REFERENCES server_has_permissions (id) ON DELETE CASCADE,
    UNIQUE (server_id, permission_id)
);


CREATE TABLE member_roles
(
    id        BIGSERIAL PRIMARY KEY,
    server_id BIGSERIAL NOT NULL REFERENCES servers (id) ON DELETE CASCADE,
    member_id BIGSERIAL NOT NULL REFERENCES server_members (id) ON DELETE CASCADE,
    role_id   BIGSERIAL NOT NULL REFERENCES server_roles (id) ON DELETE CASCADE,
    UNIQUE (server_id, member_id, role_id)
);

CREATE TABLE channel_messages
(
    id          BIGINT PRIMARY KEY,
    channel_id  BIGSERIAL NOT NULL REFERENCES channels (id) ON DELETE CASCADE,
    author_id   BIGSERIAL NOT NULL REFERENCES server_members (id),
    client_unique_id VARCHAR(100) NOT NULL UNIQUE,
    content     TEXT,
    -- List of jsonb objects with attachment info
    attachments JSONB,
    meta_data   JSONB, -- For reactions, embeds, etc.
    is_pinned   BOOLEAN                  DEFAULT FALSE,
    is_edited   BOOLEAN                  DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE,
    is_deleted  BOOLEAN                  DEFAULT FALSE
);

CREATE TABLE conversations
(
    id         BIGSERIAL PRIMARY KEY,
    is_group   BOOLEAN                  DEFAULT FALSE,
    name       VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    is_active  BOOLEAN NOT NULL         DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL         DEFAULT FALSE
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
    attachments     JSONB,
    meta_data       JSONB, -- For reactions, embeds, etc.
    is_pinned       BOOLEAN                  DEFAULT FALSE,
    is_edited       BOOLEAN                  DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE,
    is_deleted      BOOLEAN   NOT NULL       DEFAULT FALSE
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


-- ==========================================
-- DATA INITIALIZATION
-- ==========================================

-- Insert default permissions

INSERT INTO permissions (name, description)
VALUES ('FULL_ACCESS', 'Permission to have full access to all features'),
       ('CREATE_SERVER', 'Permission to create a new server'),
       ('DELETE_SERVER', 'Permission to delete a server'),
       ('MANAGE_SERVER', 'Permission to manage server settings'),
       ('CREATE_CHANNEL', 'Permission to create channels in a server'),
       ('DELETE_CHANNEL', 'Permission to delete channels in a server'),
       ('MANAGE_CHANNEL', 'Permission to manage channel settings'),
       ('SEND_MESSAGES', 'Permission to send messages in channels'),
       ('DELETE_MESSAGES', 'Permission to delete messages in channels'),
       ('BAN_USERS', 'Permission to ban users from the server'),
       ('KICK_USERS', 'Permission to kick users from the server');

INSERT INTO roles (name, description)
VALUES ('ADMIN', 'Administrator with full permissions'),
       ('MODERATOR', 'Moderator with limited management permissions'),
       ('USER', 'Regular user with standard permissions');

INSERT INTO role_permissions (role_id, permission_id)
VALUES ((SELECT id FROM roles WHERE name = 'ADMIN'),
        (SELECT id FROM permissions WHERE name = 'FULL_ACCESS'));

-- Sample user & admin account
INSERT INTO Users (username, password)
VALUES ('admin@gmail.com', '$2a$10$NL.fF5iJyANZKrvzuCjUT.V7DQrFE5oddrZ1vIouVi07UimJ2tX1y'),
       ('user1@gmail.com', '$2a$10$LQC60YO.ZW1AYMMcKEkxfuaKSjK4rSTAYV1r28eThu8IHfVgrQWI.');

INSERT INTO user_profiles (user_id, display_name, date_of_birth, avatar_url, bio)
VALUES ((SELECT id FROM users WHERE username = 'admin@gmail.com'),
        'Admin',
        '1990-01-01',
        'https://example.com/avatars/admin.png',
        'I am the administrator of this platform.'),
       ((SELECT id FROM users WHERE username = 'user1@gmail.com'),
        'User One',
        '1995-05-15',
        'https://example.com/avatars/user1.png',
        'Hello! I am User One.');

INSERT INTO user_roles (user_id, role_id)
VALUES ((SELECT id FROM users WHERE username = 'admin@gmail.com'),
        (SELECT id FROM roles WHERE name = 'ADMIN')),
       ((SELECT id FROM users WHERE username = 'user1@gmail.com'),
        (SELECT id FROM roles WHERE name = 'USER'));

-- Discord-like server permissions with bitmask values (20 most common ones)
INSERT INTO server_permissions (name, description, bitmask)
VALUES ('VIEW_CHANNEL', 'Permission to view channels', 1),
       ('SEND_MESSAGES', 'Permission to send messages in channels', 2),
       ('MANAGE_MESSAGES', 'Permission to manage messages in channels', 4),
       ('MANAGE_CHANNELS', 'Permission to manage channel settings', 8),
       ('BAN_MEMBERS', 'Permission to ban members from the server', 16),
       ('KICK_MEMBERS', 'Permission to kick members from the server', 32),
       ('MANAGE_ROLES', 'Permission to manage roles within the server', 64),
       ('MANAGE_SERVER', 'Permission to manage server settings', 128),
       ('MENTION_EVERYONE', 'Permission to mention everyone in the server', 256),
       ('MANAGE_NICKNAMES', 'Permission to manage nicknames of members', 512),
       ('CHANGE_NICKNAME', 'Permission to change own nickname', 1024),
       ('MANAGE_EMOJIS', 'Permission to manage emojis in the server', 2048),
       ('VIEW_AUDIT_LOG', 'Permission to view the audit log', 4096),
       ('PRIORITY_SPEAKER', 'Permission to use priority speaker in voice channels', 8192),
       ('STREAM', 'Permission to stream in voice channels', 16384),
       ('CONNECT', 'Permission to connect to voice channels', 32768),
       ('SPEAK', 'Permission to speak in voice channels', 65536),
       ('MUTE_MEMBERS', 'Permission to mute members in voice channels', 131072),
       ('DEAFEN_MEMBERS', 'Permission to deafen members in voice channels', 262144),
       ('MOVE_MEMBERS', 'Permission to move members between voice channels', 524288);

-- Sample server
INSERT INTO servers (owner_id, name, description, is_public)
VALUES ((SELECT id FROM users WHERE username = 'admin@gmail.com'),
        'Admin Server',
        'A server created by the admin user.',
        TRUE);

-- Sample Category
INSERT INTO categories (server_id, name, position)
VALUES ((SELECT id FROM servers WHERE name = 'Admin Server'),
        'General',
        0);

-- Sample Channels
INSERT INTO channels (server_id, category_id, name, type, position)
VALUES ((SELECT id FROM servers WHERE name = 'Admin Server'),
        (SELECT id
         FROM categories
         WHERE name = 'General'
           AND server_id = (SELECT id FROM servers WHERE name = 'Admin Server')),
        'general-chat',
        'TEXT',
        0),
       ((SELECT id FROM servers WHERE name = 'Admin Server'),
        (SELECT id
         FROM categories
         WHERE name = 'General'
           AND server_id = (SELECT id FROM servers WHERE name = 'Admin Server')),
        'voice-chat',
        'VOICE',
        1);

-- Add admin as server member
INSERT INTO server_members (server_id, user_id, nickname)
VALUES ((SELECT id FROM servers WHERE name = 'Admin Server'),
        (SELECT id FROM users WHERE username = 'admin@gmail.com'),
        'Admin');

-- Add user to server members
INSERT INTO server_members (server_id, user_id, nickname)
VALUES ((SELECT id FROM servers WHERE name = 'Admin Server'),
        (SELECT id FROM users WHERE username = 'user1@gmail.com'),
        'User One');

-- Add sample 50 message to general-chat for admin
CREATE SEQUENCE IF NOT EXISTS channel_messages_id_seq START 1000;

DO
$$
    DECLARE
        channel_id BIGINT;
        author_id  BIGINT;
    BEGIN
        SELECT id
        INTO channel_id
        FROM channels
        WHERE name = 'general-chat'
          AND server_id = (SELECT id FROM servers WHERE name = 'Admin Server');
        SELECT id
        INTO author_id
        FROM server_members
        WHERE user_id = (SELECT id FROM users WHERE username = 'admin@gmail.com')
          AND server_id = (SELECT id FROM servers WHERE name = 'Admin Server');
        FOR i IN 1..50
            LOOP
                INSERT INTO channel_messages (id, channel_id, author_id, client_unique_id, content)
                VALUES (nextval('channel_messages_id_seq'),
                        channel_id,
                        author_id,
                        nextval('channel_messages_id_seq')::TEXT,
                        'Sample message number ' || i);
            END LOOP;
    END
$$;

-- Add sample 50 message to general-chat for user1
DO
$$
    DECLARE
        channel_id BIGINT;
        author_id  BIGINT;
    BEGIN
        SELECT id
        INTO channel_id
        FROM channels
        WHERE name = 'general-chat'
          AND server_id = (SELECT id FROM servers WHERE name = 'Admin Server');
        SELECT id
        INTO author_id
        FROM server_members
        WHERE user_id = (SELECT id FROM users WHERE username = 'user1@gmail.com')
          AND server_id = (SELECT id FROM servers WHERE name = 'Admin Server');
        FOR i IN 1..50
            LOOP
                INSERT INTO channel_messages (id, channel_id, author_id, client_unique_id, content)
                VALUES (nextval('channel_messages_id_seq'),
                        channel_id,
                        author_id,
                        nextval('channel_messages_id_seq')::TEXT,
                        'User1 message number ' || i);
            END LOOP;
    END
$$;