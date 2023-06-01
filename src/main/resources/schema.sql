drop table if exists FILMS cascade;
drop table if exists users cascade;
drop table if exists MPAS cascade;
drop table if exists LIKES cascade;
drop table if exists GENRES cascade;
drop table if exists FRIENDSHIP cascade;
drop table if exists FILMS_GENRES cascade;
drop table if exists FILMS_DIRECTORS cascade;
drop table if exists DIRECTORS cascade;
drop table if exists REVIEWS cascade;
drop table if exists LIKES_REVIEWS cascade;
drop table if exists EVENTS cascade;
drop table if exists EVENT_TYPE cascade;
drop table if exists OPERATION cascade;

CREATE TABLE IF NOT EXISTS GENRES
(
    GENRE_ID   LONG auto_increment,
    GENRE_NAME CHARACTER VARYING not null,
    constraint LIST_OF_GENRES_PK
        primary key (GENRE_ID)
);

CREATE TABLE IF NOT EXISTS MPAS
(
    MPA_ID   LONG auto_increment,
    MPA_NAME CHARACTER VARYING not null,
    constraint LIST_OF_MPAS_PK
        primary key (MPA_ID)
);

create table IF NOT EXISTS DIRECTORS
(
    DIRECTOR_ID   LONG auto_increment,
    DIRECTOR_NAME CHARACTER VARYING not null,
    constraint LIST_OF_DIRECTORS_PK
        primary key (DIRECTOR_ID)
);

CREATE TABLE IF NOT EXISTS FILMS
(
    FILM_ID      LONG auto_increment,
    FILM_NAME    CHARACTER VARYING      not null,
    DESCRIPTION  CHARACTER VARYING(200) not null,
    RELEASE_DATE DATE                   not null,
    DURATION     INTEGER                not null,
    MPA_ID       LONG,
    constraint FILMS_PK
        primary key (FILM_ID),
    constraint FILMS_LIST_OF_MPAS_MPA_ID_FK
        foreign key (MPA_ID) references MPAS
);

CREATE TABLE IF NOT EXISTS FILMS_GENRES
(
    FILM_ID  LONG not null,
    GENRE_ID LONG not null,
    constraint GENRES_PK
        primary key (FILM_ID, GENRE_ID),
    constraint GENRES_FILMS_FILM_ID_FK
        foreign key (FILM_ID) references FILMS ON DELETE CASCADE,
    constraint GENRES_LIST_OF_GENRES_GENRE_ID_FK
        foreign key (GENRE_ID) references GENRES ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS FILMS_DIRECTORS
(
    DIRECTOR_ID LONG not null,
    FILM_ID     LONG not null,
    constraint DIRECTORS_PK
        primary key (DIRECTOR_ID, FILM_ID, DIRECTOR_ID),
    constraint DIRECTORS_FILMS_FILM_ID_FK
        foreign key (FILM_ID) references FILMS ON DELETE CASCADE,
    constraint DIRECTORS_LIST_OF_DIRECTORS_DIRECTOR_ID_FK
        foreign key (DIRECTOR_ID) references DIRECTORS ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS USERS
(
    USER_ID   LONG auto_increment,
    EMAIL     CHARACTER VARYING not null,
    LOGIN     CHARACTER VARYING not null,
    USER_NAME CHARACTER VARYING,
    BIRTHDAY  DATE              not null,
    constraint USERS_PK
        primary key (USER_ID)
);

CREATE TABLE IF NOT EXISTS LIKES
(
    FILM_ID LONG not null,
    USER_ID LONG not null,
    constraint LIKES_PK
        primary key (FILM_ID, USER_ID),
    constraint LIKES_FILMS_FILM_ID_FK
        foreign key (FILM_ID) references FILMS ON DELETE CASCADE,
    constraint LIKES_USERS_USER_ID_FK
        foreign key (USER_ID) references USERS ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS FRIENDSHIP
(
    USER1_ID LONG           not null,
    USER2_ID LONG           not null,
    STATUS   CHARACTER VARYING not null,
    constraint FRIENDSHIP_PK
        primary key (USER1_ID, USER2_ID),
    constraint FRIENDSHIP_USERS_USER1_ID_FK
        foreign key (USER1_ID) references USERS ON DELETE CASCADE,
    constraint FRIENDSHIP_USERS_USER2_ID_FK_2
        foreign key (USER2_ID) references USERS ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS REVIEWS
(
    REVIEW_ID   LONG auto_increment,
    USER_ID     LONG                NOT NULL,
    FILM_ID     LONG                NOT NULL,
    CONTENT     CHARACTER VARYING(200) NOT NULL,
    IS_POSITIVE BOOLEAN,
    USEFUL      INTEGER DEFAULT 0,
    constraint REVIEWS_PK
        primary key (REVIEW_ID),
    constraint REVIEWS_FILM_ID_FK
        foreign key (FILM_ID) references FILMS ON DELETE CASCADE,
    constraint REVIEWS_USER_ID_FK
        foreign key (USER_ID) references USERS ON DELETE CASCADE

);

CREATE TABLE IF NOT EXISTS LIKES_REVIEWS
(
    REVIEW_ID LONG NOT NULL,
    USER_ID   LONG NOT NULL,
    EVAL      TINYINT NOT NULL DEFAULT 1,
    constraint LIKES_REVIEWS_PK
        primary key (REVIEW_ID, USER_ID),
    constraint LIKES_REVIEWS_REVIEW_ID_FK
        foreign key (REVIEW_ID) references REVIEWS ON DELETE CASCADE,
    constraint LIKES_REVIEWS_USER_ID_FK
        foreign key (USER_ID) references USERS ON DELETE CASCADE
);

create table IF NOT EXISTS EVENT_TYPE
(
    TYPE_ID   LONG auto_increment,
    TYPE_NAME CHARACTER VARYING not null,
    constraint EVENT_TYPE_PK
        primary key (TYPE_ID)
);

create table IF NOT EXISTS OPERATION
(
    OPERATION_ID   LONG auto_increment,
    OPERATION_NAME CHARACTER VARYING not null,
    constraint OPERATION_PK
        primary key (OPERATION_ID)
);

create table IF NOT EXISTS EVENTS
(
    EVENT_ID     LONG GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    TIME_EVENT   TIMESTAMP not null,
    USER_ID      LONG   not null,
    TYPE_ID      LONG   not null,
    OPERATION_ID LONG   not null,
    ENTITY_ID    LONG   not null,
    constraint USER_ID_FK
        foreign key (USER_ID) references USERS ON DELETE CASCADE,
    constraint TYPE_ID_FK_2
        foreign key (TYPE_ID) references EVENT_TYPE,
    constraint OPERATION_ID_FK
        foreign key (OPERATION_ID) references OPERATION
);