# --- !Ups

ALTER TABLE "MenuPerDayPerPerson" ADD COLUMN "isAttending" boolean DEFAULT false;

# --- !Downs

ALTER TABLE "MenuPerDayPerPerson" DROP COLUMN "isAttending";
