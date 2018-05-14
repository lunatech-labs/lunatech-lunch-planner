# --- !Ups

CREATE OR REPLACE FUNCTION remove_nl_emails() RETURNS void AS $$
DECLARE
  fruser "User"%rowtype;;
BEGIN
  FOR fruser IN
  SELECT  *
  FROM    "User"
  WHERE   "emailAddress" like '%@lunatech.nl'
  LOOP
    DELETE FROM "MenuPerDayPerPerson" where "userUuid" = fruser.uuid;;
    DELETE FROM "UserProfile" where "userUuid" = fruser.uuid;;
    DELETE FROM "User" where "uuid" = fruser.uuid;;
  END LOOP;;
  RETURN;;
END
$$ language 'plpgsql';;

select remove_nl_emails();

# --- !Downs

