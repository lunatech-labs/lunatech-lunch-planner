#!/usr/bin/env bash
createuser --username "$POSTGRES_USER" lunch-planner --superuser
createdb --username "$POSTGRES_USER" -O lunch-planner lunch-planner

psql -U lunch-planner -d lunch-planner -c 'CREATE EXTENSION IF NOT EXISTS HSTORE;'
