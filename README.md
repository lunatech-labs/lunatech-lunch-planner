This is the source code for the Lunatech Lunch Planner application
=====================================

This is a Play Framework 2 application.
The application will allow Lunatech do manage friday lunches' menus and the people that are attending those lunches.

###To get started:

Compile and run the docker container:

In `/dockerdev/postgres` run

```
docker build -t lunatech-lunch-planner .
```

```
docker run -it --rm -m 1024m -p 5432:5432 lunatech-lunch-planner
```

Start the app:

```
sbt run
```

####Docker compose
You can also spin a docker image with:
```
sbt dockerComposeUp
```

and then
```
sbt test
```
OR
```
sbt run
```

You can stop the container with:
```
sbt dockerComposeStop
```

Open the browser and you are all set:
```
localhost:9000
```
