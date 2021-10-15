# --- !Ups

ALTER TABLE "Dish" ADD COLUMN "isHalal" Boolean DEFAULT false;
ALTER TABLE "UserProfile" ADD COLUMN "halal" Boolean DEFAULT false;

# --- !Downs

ALTER TABLE "Dish" DROP COLUMN "isHalal";
ALTER TABLE "UserProfile" DROP COLUMN "halal";
