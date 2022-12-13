create database if not exists teamup character set utf8;
use teamup;
CREATE TABLE user (
    id bigint(20) primary key auto_increment,
    name varchar(32),
    email varchar(32),
    password  varchar(32),
    teams varchar(255)
);

CREATE TABLE team (
    id bigint(20) primary key auto_increment,
    creator_id bigint(20),
    name varchar(32),
    teammates VARCHAR(255),
    info_id bigint(20),
    commentlist VARCHAR(255)
);

CREATE TABLE comment (
    id bigint(20) primary key auto_increment,
    sender_id bigint(20),
    team_id bigint(20),
    date VARCHAR(32),
    content varchar(255)
);

CREATE TABLE info (
    id bigint(20) primary key auto_increment,
    course varchar(32),
    team_id bigint(20),
    number_limit int(11),
    content varchar(255)
);

CREATE TABLE application (
    id bigint(20) primary key auto_increment,
    uid bigint(20),
    tid bigint(20),
    msg VARCHAR(255),
    state bigint(20)
);