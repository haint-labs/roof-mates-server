create table if not exists regions (
	id serial primary key,
	address varchar
);

create table if not exists spots (
	id serial primary key,
	region_id serial references regions(id) on delete cascade,
	parking_number int
);

create type device_os as enum ('android', 'ios');

create table if not exists devices (
    id serial primary key,
    device_os device_os,
    token varchar
);

create type user_type as enum ('guest', 'owner');

create table if not exists users (
    id serial primary key,
    type user_type,
    name varchar,
    surname varchar,
    phone_number varchar,
    device_id serial references devices(id) on delete cascade,
    spot_id serial references spots(id) on delete cascade
);
