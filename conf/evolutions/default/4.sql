# --- !Ups

CREATE TABLE "MenuPerDay" (
  uuid UUID NOT NULL,
  menuUUID UUID NOT NULL,
  date Date NOT NULL,
  CONSTRAINT menuPerDay_pkey_ PRIMARY KEY (uuid)
);

# --- !Downs

DROP TABLE "MenuPerDay";
