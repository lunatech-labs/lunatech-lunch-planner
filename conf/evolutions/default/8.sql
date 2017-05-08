# --- !Ups

ALTER TABLE "User" ADD COLUMN "isAdmin" boolean DEFAULT false;

# --- !Downs

ALTER TABLE "User" DROP COLUMN "isAdmin";
