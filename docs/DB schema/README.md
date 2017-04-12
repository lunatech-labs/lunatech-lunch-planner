The DB diagram was built using `https://www.draw.io/`

The DB is composed by 6 tables:
- User
- Dish
- Menu
- MenuDish
- MenuPerDay
- MenuPerDayPerPerson

**User table**

Whenever a new user logs in the application (using Google signin) a uuid is created for the user and saved in the DB, together with the name and email.
The user uuid will be need later to trace how many and who is attending a certain meal on a certain day.

**Dish table**

This table holds the information of all the dishes available and it's detailed information.

**Menu table**

This table holds the name and uuid of a menu.

**MenuDish table**

This tables makes the bridge between a menu and the dishes that are part of that menu.
Different menus can share the same dishes.

**MenuPerDay table**

This table holds the information about the day(s) a certain menu will be served.
The same menu can be served in different days.

**MenuPerDayPerPerson**

This table holds the information of the users that have said that day are attending a certain menu on a certain day (MenuPerDay).
