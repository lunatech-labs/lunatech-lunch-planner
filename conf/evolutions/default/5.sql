# --- !Ups

CREATE TABLE "MenuPerDayPerPerson" (
  uuid UUID NOT NULL,
  menuPerDayUUID UUID NOT NULL,
  userUUID UUID NOT NULL,
  CONSTRAINT menuPerDayPerPerson_pkey_ PRIMARY KEY (uuid)
);

# --- !Downs

DROP TABLE "MenuPerDayPerPerson";
