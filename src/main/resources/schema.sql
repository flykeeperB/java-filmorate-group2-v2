CREATE TABLE IF NOT EXISTS LIST_OF_GENRES
(
    GENRE_ID   INTEGER auto_increment,
    GENRE_NAME CHARACTER VARYING not null,
    constraint LIST_OF_GENRES_PK
        primary key (GENRE_ID)
);

CREATE TABLE IF NOT EXISTS LIST_OF_MPAS
(
    MPA_ID   INTEGER not null,
    MPA_NAME CHARACTER VARYING not null,
    constraint LIST_OF_MPAS_PK
        primary key (MPA_ID)
);

CREATE TABLE IF NOT EXISTS FILMS
(
    FILM_ID      INTEGER auto_increment,
    FILM_NAME    CHARACTER VARYING      not null,
    DESCRIPTION  CHARACTER VARYING(200) not null,
    RELEASE_DATE DATE                   not null,
    DURATION     INTEGER                not null,
    MPA_ID          INTEGER,
    constraint FILMS_PK
        primary key (FILM_ID),
    constraint FILMS_LIST_OF_MPAS_MPA_ID_FK
        foreign key (MPA_ID) references LIST_OF_MPAS
);

CREATE TABLE IF NOT EXISTS GENRES
(
    FILM_ID  int not null,
    GENRE_ID int not null,
    constraint GENRES_PK
        primary key (FILM_ID, GENRE_ID),
    constraint GENRES_FILMS_FILM_ID_FK
        foreign key (FILM_ID) references FILMS,
    constraint GENRES_LIST_OF_GENRES_GENRE_ID_FK
        foreign key (GENRE_ID) references LIST_OF_GENRES
);

CREATE TABLE IF NOT EXISTS USERS
(
    USER_ID      INTEGER auto_increment,
    EMAIL        CHARACTER VARYING      not null,
    LOGIN        CHARACTER VARYING      not null,
    USER_NAME    CHARACTER VARYING,
    BIRTHDAY     DATE                   not null,
    constraint USERS_PK
        primary key (USER_ID)
);

CREATE TABLE IF NOT EXISTS LIKES
(
    FILM_ID INTEGER not null,
    USER_ID INTEGER not null,
    constraint LIKES_PK
        primary key (FILM_ID, USER_ID),
    constraint LIKES_FILMS_FILM_ID_FK
        foreign key (FILM_ID) references FILMS,
    constraint LIKES_USERS_USER_ID_FK
        foreign key (USER_ID) references USERS
);

CREATE TABLE IF NOT EXISTS FRIENDSHIP
(
    USER1_ID  INTEGER           not null,
    USER2_ID  INTEGER           not null,
    STATUS    CHARACTER VARYING not null,
    constraint FRIENDSHIP_PK
        primary key (USER1_ID, USER2_ID),
    constraint FRIENDSHIP_USERS_USER1_ID_FK
        foreign key (USER1_ID) references USERS,
    constraint FRIENDSHIP_USERS_USER2_ID_FK_2
        foreign key (USER2_ID) references USERS
);

CREATE TABLE IF NOT EXISTS REVIEWS
(
    REVIEW_ID   INTEGER auto_increment,
    USER_ID     INTEGER NOT NULL ,
    FILM_ID     INTEGER NOT NULL ,
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
    REVIEW_ID   INTEGER NOT NULL,
    USER_ID     INTEGER NOT NULL,
    EVAL     TINYINT NOT NULL DEFAULT 1,
    constraint LIKES_REVIEWS_PK
        primary key (REVIEW_ID, USER_ID),
    constraint LIKES_REVIEWS_REVIEW_ID_FK
        foreign key (REVIEW_ID) references REVIEWS ON DELETE CASCADE,
    constraint LIKES_REVIEWS_USER_ID_FK
        foreign key (USER_ID) references USERS ON DELETE CASCADE
);