# --- !Ups
CREATE TABLE `jobs` (

	`main_id`             INTEGER UNSIGNED    NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`type`                VARCHAR(50),
	`parent_id`           INTEGER,
	`job_id`              VARCHAR(100),
	`user_id`             INTEGER,
	`status`              CHAR(1),
	`tool`                VARCHAR(100),
	`stat_id`             INTEGER,
	`created_on`          TIMESTAMP           DEFAULT CURRENT_TIMESTAMP,
	`updated_on`          TIMESTAMP           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`viewed_on`           TIMESTAMP           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

# --- !Downs
DROP TABLE `jobs`;