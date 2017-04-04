# --- !Ups

CREATE TABLE "MenuPerDay" (
  uuid UUID NOT NULL,
  "menuUuid" UUID NOT NULL,
  date Date NOT NULL,
  CONSTRAINT menuPerDay_pkey_ PRIMARY KEY (uuid),
  CONSTRAINT menuPerDayMenu_fkey_ FOREIGN KEY ("menuUuid") REFERENCES "Menu" ("uuid") MATCH FULL
);

# --- !Downs

DROP TABLE "MenuPerDay";
