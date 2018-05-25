# --- !Ups

ALTER TABLE "Dish" ADD COLUMN "isDeleted" Boolean DEFAULT false;
ALTER TABLE "Menu" ADD COLUMN "isDeleted" Boolean DEFAULT false;
ALTER TABLE "MenuDish" ADD COLUMN "isDeleted" Boolean DEFAULT false;
ALTER TABLE "MenuPerDay" ADD COLUMN "isDeleted" Boolean DEFAULT false;
ALTER TABLE "User" ADD COLUMN "isDeleted" Boolean DEFAULT false;
ALTER TABLE "UserProfile" ADD COLUMN "isDeleted" Boolean DEFAULT false;

# --- !Downs

ALTER TABLE "Dish" DROP COLUMN "isDeleted";
ALTER TABLE "Menu" DROP COLUMN "isDeleted";
ALTER TABLE "MenuDish" DROP COLUMN "isDeleted";
ALTER TABLE "MenuPerDay" DROP COLUMN "isDeleted";
ALTER TABLE "User" DROP COLUMN "isDeleted";
ALTER TABLE "UserProfile" DROP COLUMN "isDeleted";
