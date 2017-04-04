# --- !Ups

CREATE TABLE "User" (
  uuid UUID NOT NULL,
  name TEXT NOT NULL,
  emailaddress TEXT NOT NULL,
  isadmin BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT user_pkey_ PRIMARY KEY (uuid)
);


# --- !Downs

DROP TABLE "User";
