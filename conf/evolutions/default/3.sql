# --- !Ups

CREATE TABLE "Menu" (
  uuid UUID NOT NULL,
  name TEXT NOT NULL,
  CONSTRAINT menu_pkey_ PRIMARY KEY (uuid)
);

# --- !Downs

DROP TABLE "Menu";
