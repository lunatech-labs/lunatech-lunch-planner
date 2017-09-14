# --- !Ups

ALTER TABLE "MenuPerDay" ADD COLUMN "location" TEXT DEFAULT 'Rotterdam';

# --- !Downs

ALTER TABLE "MenuPerDay" DROP COLUMN "location";