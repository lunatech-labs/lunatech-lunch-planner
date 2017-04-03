# --- !Ups

INSERT INTO "User" VALUES ('563350FA-B077-4D6E-A8B8-75D9C76BD11B','Leonor Boga','leonor.boga@lunatech.com', true)

# --- !Downs
DELETE FROM "User" WHERE uuid = '563350FA-B077-4D6E-A8B8-75D9C76BD11B'
