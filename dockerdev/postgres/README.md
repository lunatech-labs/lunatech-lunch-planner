To create docker image:
docker build -t lunatech-lunch-planner .

To run docker image:
docker run -it --rm -m 1024m --name postgres -p 5432:5432 lunatech-lunch-planner
