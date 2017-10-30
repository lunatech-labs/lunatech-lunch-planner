# --- !Ups
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
insert into "User"(uuid, name, "emailAddress", "isAdmin") VALUES (uuid_generate_v1(), 'Jane Doe', 'jane.doe@lunatech.com', FALSE);
insert into "User"(uuid, name, "emailAddress", "isAdmin") VALUES (uuid_generate_v1(), 'John Doe', 'john.doe@lunatech.com', FALSE);
insert into "User"(uuid, name, "emailAddress", "isAdmin") VALUES (uuid_generate_v1(), 'Johnny Doe', 'johnny.doe@lunatech.com', FALSE);
insert into "User"(uuid, name, "emailAddress", "isAdmin") VALUES (uuid_generate_v1(), 'Janie Doe', 'janie.doe@lunatech.com', FALSE);

INSERT INTO public."UserProfile" ("userUuid", vegetarian, "seaFoodRestriction", "porkRestriction", "beefRestriction", "chickenRestriction", "glutenRestriction", "lactoseRestriction", "otherRestriction")
VALUES ((select uuid from "User" where "emailAddress" = 'jane.doe@lunatech.com'), false, false, false, false, false, false, false, '');

INSERT INTO public."UserProfile" ("userUuid", vegetarian, "seaFoodRestriction", "porkRestriction", "beefRestriction", "chickenRestriction", "glutenRestriction", "lactoseRestriction", "otherRestriction")
VALUES ((select uuid from "User" where "emailAddress" = 'john.doe@lunatech.com'), false, false, false, false, false, false, false, '');

INSERT INTO public."UserProfile" ("userUuid", vegetarian, "seaFoodRestriction", "porkRestriction", "beefRestriction", "chickenRestriction", "glutenRestriction", "lactoseRestriction", "otherRestriction")
VALUES ((select uuid from "User" where "emailAddress" = 'johnny.doe@lunatech.com'), false, false, false, false, false, false, false, '');

INSERT INTO public."UserProfile" ("userUuid", vegetarian, "seaFoodRestriction", "porkRestriction", "beefRestriction", "chickenRestriction", "glutenRestriction", "lactoseRestriction", "otherRestriction")
VALUES ((select uuid from "User" where "emailAddress" = 'janie.doe@lunatech.com'), false, false, false, false, false, false, false, '');

INSERT INTO public."Dish" (uuid, name, description, "isVegetarian", "hasSeaFood", "hasPork", "hasBeef", "hasChicken", "isGlutenFree", "hasLactose", remarks) VALUES (uuid_generate_v1(), 'sushi', 'sushi desc', false, false, false, false, false, false, false, null);
INSERT INTO public."Menu" (uuid, name) VALUES (uuid_generate_v1(), 'sushi menu');
INSERT INTO public."MenuDish" (uuid, "menuUuid", "dishUuid") VALUES (uuid_generate_v1(), (select uuid from "Menu" where name = 'sushi menu'), (select uuid from "Dish" where name = 'sushi'));

INSERT INTO public."MenuPerDay" (uuid, "menuUuid", date, location) VALUES (uuid_generate_v1(), (select uuid from "Menu" WHERE name = 'sushi menu'), '2017-11-03', 'Rotterdam');
INSERT INTO public."MenuPerDay" (uuid, "menuUuid", date, location) VALUES (uuid_generate_v1(), (select uuid from "Menu" WHERE name = 'sushi menu'), '2017-11-10', 'Rotterdam');
INSERT INTO public."MenuPerDay" (uuid, "menuUuid", date, location) VALUES (uuid_generate_v1(), (select uuid from "Menu" WHERE name = 'sushi menu'), '2017-11-03', 'Amsterdam');

INSERT INTO public."MenuPerDayPerPerson" (uuid, "menuPerDayUuid", "userUuid", "isAttending") VALUES (uuid_generate_v1(), (select uuid from "MenuPerDay" WHERE date = '2017-11-03' and location = 'Rotterdam'),
                                                                                                     (select uuid from "User" where "emailAddress" = 'john.doe@lunatech.com'), TRUE);
INSERT INTO public."MenuPerDayPerPerson" (uuid, "menuPerDayUuid", "userUuid", "isAttending") VALUES (uuid_generate_v1(), (select uuid from "MenuPerDay" WHERE date = '2017-11-03' and location = 'Rotterdam'),
                                                                                                     (select uuid from "User" where "emailAddress" = 'jane.doe@lunatech.com'), TRUE);
INSERT INTO public."MenuPerDayPerPerson" (uuid, "menuPerDayUuid", "userUuid", "isAttending") VALUES (uuid_generate_v1(), (select uuid from "MenuPerDay" WHERE date = '2017-11-03' and location = 'Rotterdam'),
                                                                                                     (select uuid from "User" where "emailAddress" = 'johnny.doe@lunatech.com'), TRUE);
INSERT INTO public."MenuPerDayPerPerson" (uuid, "menuPerDayUuid", "userUuid", "isAttending") VALUES (uuid_generate_v1(), (select uuid from "MenuPerDay" WHERE date = '2017-11-03' and location = 'Rotterdam'),
                                                                                                     (select uuid from "User" where "emailAddress" = 'janie.doe@lunatech.com'), FALSE);
INSERT INTO public."MenuPerDayPerPerson" (uuid, "menuPerDayUuid", "userUuid", "isAttending") VALUES (uuid_generate_v1(), (select uuid from "MenuPerDay" WHERE date = '2017-11-10' and location = 'Rotterdam'),
                                                                                                     (select uuid from "User" where "emailAddress" = 'john.doe@lunatech.com'), TRUE);
INSERT INTO public."MenuPerDayPerPerson" (uuid, "menuPerDayUuid", "userUuid", "isAttending") VALUES (uuid_generate_v1(), (select uuid from "MenuPerDay" WHERE date = '2017-11-10' and location = 'Rotterdam'),
                                                                                                     (select uuid from "User" where "emailAddress" = 'jane.doe@lunatech.com'), FALSE);
INSERT INTO public."MenuPerDayPerPerson" (uuid, "menuPerDayUuid", "userUuid", "isAttending") VALUES (uuid_generate_v1(), (select uuid from "MenuPerDay" WHERE date = '2017-11-03' and location = 'Amsterdam'),
                                                                                                     (select uuid from "User" where "emailAddress" = 'janie.doe@lunatech.com'), TRUE);

# --- !Downs
DROP EXTENSION "uuid-ossp";

delete from "MenuPerDayPerPerson" WHERE "userUuid" = (select uuid from "User" where "emailAddress" = 'john.doe@lunatech.com');
delete from "MenuPerDayPerPerson" WHERE "userUuid" = (select uuid from "User" where "emailAddress" = 'jane.doe@lunatech.com');
delete from "MenuPerDayPerPerson" WHERE "userUuid" = (select uuid from "User" where "emailAddress" = 'johnny.doe@lunatech.com');
delete from "MenuPerDayPerPerson" WHERE "userUuid" = (select uuid from "User" where "emailAddress" = 'janie.doe@lunatech.com');

delete FROM "MenuPerDay" where date = '2017-11-03' and location = 'Rotterdam' AND "menuUuid" = (select uuid from "Menu" WHERE name = 'sushi menu');
delete FROM "MenuPerDay" where date = '2017-11-10' and location = 'Rotterdam' AND "menuUuid" = (select uuid from "Menu" WHERE name = 'sushi menu');

delete FROM "MenuDish" where "menuUuid" = (select uuid from "Menu" where name = 'sushi menu');
delete FROM "MenuDish" where "dishUuid" = (select uuid from "Dish" where name = 'sushi');

delete FROM "Dish" where name = 'sushi';
delete FROM "Menu" where name = 'sushi menu';

delete from "UserProfile" where "userUuid" = (select uuid from "User" where "emailAddress" = 'jane.doe@lunatech.com');
delete from "UserProfile" where "userUuid" = (select uuid from "User" where "emailAddress" = 'john.doe@lunatech.com');
delete from "UserProfile" where "userUuid" = (select uuid from "User" where "emailAddress" = 'johnny.doe@lunatech.com');
delete from "UserProfile" where "userUuid" = (select uuid from "User" where "emailAddress" = 'janie.doe@lunatech.com');

delete FROM "User" where "emailAddress" = 'jane.doe@lunatech.com';
delete FROM "User" where "emailAddress" = 'john.doe@lunatech.com';
delete FROM "User" where "emailAddress" = 'johnny.doe@lunatech.com';
delete FROM "User" where "emailAddress" = 'janie.doe@lunatech.com';
