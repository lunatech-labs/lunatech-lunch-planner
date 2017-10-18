# --- !Ups

ALTER TABLE "MenuPerDayPerPerson"
    ADD COLUMN "isAttending" BOOLEAN DEFAULT false;

# --- !Downs

ALTER TABLE "MenuPerDayPerPerson"
    DROP COLUMN "isAttending";
