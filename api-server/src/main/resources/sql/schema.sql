create table if not exists room
(
    id           bigint default nextval('tg_room_id_seq'::regclass) not null
    constraint tg_room_pkey
    primary key,
    link         varchar(255)                                       not null
    constraint ux_tg_room__link
    unique,
    name         varchar(255),
    jhi_desc     text,
    member_cnt   integer,
    type         varchar(255),
    status       varchar(255)                                       not null,
    collected_at timestamp
    );

alter table room
    owner to demo;

create table if not exists message
(
    "offset"  bigint                                               not null,
    chat_id   bigint                                               not null,
    sender_id bigint                                               not null,
    content   text,
    send_time timestamp,
    id        integer default nextval('messages_id_seq'::regclass) not null
    primary key,
    constraint messages_offset_chat_id_key
    unique ("offset", chat_id)
    );

alter table message
    owner to demo;

create table if not exists offsets
(
    chat_id     bigint not null
    primary key,
    last_offset bigint not null,
    crawl_link  boolean default false,
    room_name   varchar(1024),
    link        varchar(1024)
    );

alter table offsets
    owner to demo;

create table if not exists search_log
(
    id        serial
    primary key,
    search_at timestamp,
    kw        varchar(256),
    t         varchar(256),
    p         integer,
    ip        varchar(256)
    );

alter table search_log
    owner to demo;




