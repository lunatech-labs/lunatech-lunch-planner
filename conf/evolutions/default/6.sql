# --- !Ups

CREATE TABLE "MenuPerDayPerPerson" (
  uuid UUID NOT NULL,
  "menuPerDayUuid" UUID NOT NULL,
  "userUuid" UUID NOT NULL,
  CONSTRAINT menuPerDayPerPerson_pkey_ PRIMARY KEY (uuid),
  CONSTRAINT menuPerDayPerPersonMenuPerDay_fkey_ FOREIGN KEY ("menuPerDayUuid") REFERENCES "MenuPerDay" ("uuid") MATCH FULL,
  CONSTRAINT menuPerDayPerPersonUser_fkey_ FOREIGN KEY ("userUuid") REFERENCES "User" ("uuid") MATCH FULL
);

# --- !Downs

DROP TABLE "MenuPerDayPerPerson";
