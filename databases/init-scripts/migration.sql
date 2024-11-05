CREATE EXTENSION postgis;

CREATE TABLE IF NOT EXISTS cluster (
   id SERIAL PRIMARY KEY,
   list_size INT,
   points GEOMETRY(MULTIPOINT)
);

CREATE TABLE IF NOT EXISTS data (
   id SERIAL PRIMARY KEY,
   cluster_id INT,
   FOREIGN KEY (cluster_id) REFERENCES cluster(id),
   case_number VARCHAR(100),
   date_year VARCHAR(8),
   location GEOGRAPHY(POINT)
);

CREATE TABLE IF NOT EXISTS processed_data (
   id SERIAL PRIMARY KEY,
   case_year VARCHAR(100),
   state VARCHAR(100)
);

CREATE INDEX ON data USING gist(location);