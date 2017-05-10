# --- !Ups

CREATE TABLE "UserProfile" (
  "userUuid" UUID NOT NULL,
  "vegetarian" BOOLEAN NOT NULL DEFAULT FALSE,
  "seaFoodRestriction" BOOLEAN NOT NULL DEFAULT FALSE,
  "porkRestriction" BOOLEAN NOT NULL DEFAULT FALSE,
  "beefRestriction" BOOLEAN NOT NULL DEFAULT FALSE,
  "chickenRestriction" BOOLEAN NOT NULL DEFAULT FALSE,
  "glutenRestriction" BOOLEAN NOT NULL DEFAULT FALSE,
  "lactoseRestriction" BOOLEAN NOT NULL DEFAULT FALSE,
  "otherRestriction" TEXT,
  CONSTRAINT userProfile_pkey_ PRIMARY KEY ("userUuid"),
  CONSTRAINT userProfileUser_fkey_ FOREIGN KEY ("userUuid") REFERENCES "User" ("uuid") MATCH FULL
);

# --- !Downs

DROP TABLE "UserProfile";
