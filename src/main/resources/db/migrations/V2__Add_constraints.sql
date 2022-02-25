alter table users alter column spot_id drop not null;
alter table users add unique (phone_number);

alter table regions add unique (address);

alter table spots add unique (parking_number);