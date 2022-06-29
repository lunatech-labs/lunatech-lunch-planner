[![Build Status](https://travis-ci.org/lunatech-labs/lunatech-lunch-planner.svg?branch=master)](https://travis-ci.org/lunatech-labs/lunatech-lunch-planner)

This is the source code for the Lunatech Lunch Planner application
=====================================

This is a Play Framework 2 application.
The application will allow Lunatech do manage friday lunches' menus and the people that are attending those lunches.

### To get started:

##### Compile and run the docker container:

In `/dockerdev/postgres` run

```
docker build -t lunatech-lunch-planner .
```

```
docker run -it --rm -m 1024m -p 5432:5432 lunatech-lunch-planner
```

##### Create configuration override

Create a `conf/override.conf` file with the following fields:
 
```
PLAY_SECRET = xxx

POSTGRESQL_ADDON_HOST = localhost
POSTGRESQL_ADDON_PORT = 5432
POSTGRESQL_ADDON_DB = lunch-planner
POSTGRESQL_ADDON_USER = postgres
POSTGRESQL_ADDON_PASSWORD = password

GOOGLE_CLIENTID = xxx
GOOGLE_SECRET = xxx

SLACK_API_TOKEN = xxx
SLACK_HOST_NAME = xxx

administrators = ["developer@lunatech.nl"]
``` 

Ensure that your database credentials are correct. 

##### Start the app:

```
sbt run
```

Make sure the docker image withe the DB is running. Open the browser and you are all set:
```
localhost:9000
```

##### Run the tests:

To run the tests no docker image is necessary, instead an H2 in-memory DB has been set up


### Deployment
The lunch-planner is hosted in clever-cloud. To deploy a new version just merge master branch into production branch
and an automatic deployment will be triggered.


