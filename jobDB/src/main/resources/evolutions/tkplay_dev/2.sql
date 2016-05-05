# --- !Ups
CREATE TABLE `jobs` (

	`job_id`              INTEGER UNSIGNED    NOT NULL PRIMARY KEY,
	`type`                VARCHAR(50),
	`parent_id`           INTEGER,
	`session_id`          VARCHAR(100),
	`status`              CHAR(1),
	`tool`                VARCHAR(100),
	`stat_id`             INTEGER,
	`created_on`          DATETIME,
	`updated_on`          DATETIME,
	`viewed_on`           DATETIME
);

# --- !Downs
DROP TABLE `jobs`;
