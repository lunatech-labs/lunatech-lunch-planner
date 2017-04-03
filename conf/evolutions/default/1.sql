# --- !Ups
CREATE TABLE "User" (
  uuid UUID NOT NULL DEFAULT uuid_generate_v1(),
  name TEXT NOT NULL,
  emailAddress TEXT NOT NULL,
  isAdmin BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT user_pkey_ PRIMARY KEY (uuid)
);


# --- !Downs

DROP TABLE "User";
