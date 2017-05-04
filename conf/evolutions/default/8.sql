DELETE FROM "User";
# --- !Ups

INSERT INTO "Dish" VALUES ('C710C7DA-F9FE-470B-80C8-CE8A79A9237F', 'Antipasto misto all italiana', 'Selection of Italian cured meats & cheeses with assorted roasted vegetables in extra virgin olive oil', false, false, false, true, false, false, false, '');
INSERT INTO "Dish" VALUES ('80DFE74A-B6F4-48E2-9C55-557032F6CF6F', 'Prosciutto crudo di Parma e melone', 'Slices of melon draped with cured Italian ham', false, false, false, true, false, false, false, '');
INSERT INTO "Dish" VALUES ('9F55C13B-BF6A-4B53-9B65-0D2250C3EA8B', 'Insalata tricolore', 'Tomato, mozzarella, avocado & basil', true, false, false, false, false, false, false, '');
INSERT INTO "Dish" VALUES ('6A9CC4A6-84D1-475F-BD46-0E17B09AE2EE', 'Avocado al forno', 'Baked avocado topped with tomato sauce, mozzarella and touch of chilli', true, false, false, false, false, false, false, '');
INSERT INTO "Dish" VALUES ('123A48B6-EE15-4780-BE00-39DAA605B3AC', 'Gamberoni all aglio', 'King prawns panfried in garlic, olive oil, chilli & tomato', false, true, false, false, false, false, false, '');

# --- !Downs
DELETE FROM "Dish";
