[![Build Status](https://travis-ci.org/lunatech-labs/lunatech-lunch-planner.svg?branch=master)](https://travis-ci.org/lunatech-labs/lunatech-lunch-planner)

This is the source code for the Lunatech Lunch Planner application
=====================================

This is a Play Framework 2 application.
The application will allow Lunatech do manage friday lunches' menus and the people that are attending those lunches.

### To get started:

##### Set up authentication for github packages

If you have not done so yet you have to configure authentication for github packages, because this app depends on some lunatech provided jars hosted there.

For this you need to generated a github personal access token (https://github.com/settings/tokens) with access to at least the following scope: `write:packages`.

This can be provided to the build in two ways:

1. Set the environment variable `GITHUB_TOKEN`
2. Configure it in your git config (e.g.: `$HOME/.gitconfig`) with the following command: `git config --global github.token "<your token>"`

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


### New developments
There are two permanent branches: `master` and `clevercloud`.
All new developments are to be merged in `master` branch and tested locally and then later deployed by using the `clevercloud` branch.

# Deployment
The lunch-planner is hosted in clever-cloud. The deployment is done by rebasing `clevercloud` branch onto `master` branch locally. 
This rebase will always be a fast-forward one.

Before doing a deployment make sure you have the latest versions of `master` and `production` branches. Rebase `clevercloud` onto `master` and push it. Clever-cloud will do the deployment automatically.

```
$ git checkout master
[master]$ git pull
[master]$ git checkout clevercloud
[clevercloud]$ git pull
[clevercloud]$ git rebase master
[clevercloud]$ git push
```


