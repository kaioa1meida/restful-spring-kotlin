CREATE TABLE IF NOT EXISTS person (
   id BIGINT AUTO_INCREMENT NOT NULL,
   first_name VARCHAR(255) NOT NULL,
   last_name VARCHAR(255) NOT NULL,
   address VARCHAR(255) NOT NULL,
   gender VARCHAR(255) NOT NULL,
   PRIMARY KEY (id)
);
