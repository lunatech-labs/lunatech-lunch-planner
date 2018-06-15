# --- !Ups

ALTER TABLE "MenuPerDayPerPerson"
  ADD COLUMN "timestamp" TIMESTAMP DEFAULT current_timestamp;

# --- !Downs

ALTER TABLE "MenuPerDayPerPerson"
  DROP COLUMN "timestamp";