# --- !Ups

update "User" set "emailAddress" = regexp_replace("emailAddress", '@lunatech.com', '@lunatech.nl', 'g') where "emailAddress" like '%@lunatech.com';

# --- !Downs

update "User" set "emailAddress" = regexp_replace("emailAddress", '@lunatech.nl', '@lunatech.com', 'g') where "emailAddress" like '%@lunatech.nl'
