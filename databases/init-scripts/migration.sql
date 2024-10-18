CREATE TABLE IF NOT EXISTS data (
   id SERIAL PRIMARY KEY,
   case_number VARCHAR(100),
   latitude FLOAT,
   longitude FLOAT
);