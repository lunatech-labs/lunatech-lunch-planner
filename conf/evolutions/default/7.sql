# --- !Ups

INSERT INTO "User" VALUES ('F8B64D58-9C0C-43BB-98DB-A290072D42F5', 'Leonor Boga', 'leonor.boga@lunatech.com', true);
INSERT INTO "User" VALUES ('A102D8C5-996B-4F24-9278-55C7298647FC', 'Vijay Kiran', 'vijay.kiran@lunatech.com', true);
INSERT INTO "User" VALUES ('8A04DD11-088F-4AA7-9D5E-7BB7FCAD5339', 'Developer', 'developer@lunatech.com', true);

# --- !Downs

DELETE FROM "User";
