create table if not exists spot_shares (
	id serial primary key,
	spot_id serial references spots(id) on delete cascade,
	user_id serial references users(id) on delete cascade,
	from_timestamp integer,
	to_timestamp integer
);

create table if not exists spot_bookings (
	id serial primary key,
	spot_share_id serial references spot_shares(id) on delete cascade,
	user_id serial references users(id) on delete cascade,
	from_timestamp integer,
	to_timestamp integer
);

create table if not exists spot_requests (
	id serial primary key,
	user_id serial references users(id) on delete cascade,
	regions integer array,
	from_timestamp integer,
	to_timestamp integer
);
