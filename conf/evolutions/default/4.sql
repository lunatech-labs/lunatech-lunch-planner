# --- !Ups

CREATE TABLE "MenuDish" (
  uuid UUID NOT NULL,
  "menuUuid" UUID NOT NULL,
  "dishUuid" UUID NOT NULL,
  CONSTRAINT menuDish_pkey_ PRIMARY KEY (uuid),
  CONSTRAINT menuDishMenu_fkey_ FOREIGN KEY ("menuUuid") REFERENCES "Menu" ("uuid") MATCH FULL,
  CONSTRAINT menuDishDish_fkey_ FOREIGN KEY ("dishUuid") REFERENCES "Dish" ("uuid") MATCH FULL
);

# --- !Downs

DROP TABLE "MenuDish";
