#!/bin/bash

set -e
set -u

IFS=','

TEST_DATABASE="${POSTGRES_DB}_test"
echo "Creating test database $TEST_DATABASE"
psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER $POSTGRES_DB -c "create database $TEST_DATABASE"
echo "Database $TEST_DATABASE created"

echo "Create test table"
psql --username $POSTGRES_USER $TEST_DATABASE -f /docker-entrypoint-initdb.d/migration.sql
echo "Created test tables"
