#!/usr/bin/env bash
createuser --username "$POSTGRES_USER" lunch-planner --superuser
createdb --username "$POSTGRES_USER" -O lunch-planner lunch-planner
createdb --username "$POSTGRES_USER" -O lunch-planner lunch-planner-test
