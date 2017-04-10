# --- !Ups

CREATE TABLE "User" (
  uuid UUID NOT NULL,
  name TEXT NOT NULL,
  "emailAddress" TEXT NOT NULL,
  CONSTRAINT user_pkey_ PRIMARY KEY (uuid)
);


# --- !Downs

DROP TABLE "User";
