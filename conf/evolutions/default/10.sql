# --- !Ups

INSERT INTO "UserProfile" VALUES ('8A04DD11-088F-4AA7-9D5E-7BB7FCAD5339', false, false, false, false, false, false, false, '');

# --- !Downs

DELETE FROM "UserProfile" WHERE "userUuid" = '8A04DD11-088F-4AA7-9D5E-7BB7FCAD5339';
