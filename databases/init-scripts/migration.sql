CREATE EXTENSION postgis;

CREATE TABLE IF NOT EXISTS data (
   id SERIAL PRIMARY KEY,
   case_number VARCHAR(100),
   date_year VARCHAR(8),
   location GEOGRAPHY(POINT)
);

CREATE INDEX ON data USING gist(location);