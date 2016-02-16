# --- !Ups
CREATE TABLE `jobs` (

	`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`type` varchar(100),
	`parent_id` BIGINT,
	`jobid` BIGINT,
	`user_id` BIGINT,
	`status` char(1),
	`tool` varchar(100),
	`stat_id` BIGINT,
	`created_on` DATETIME,
	`updated_on` DATETIME,
	`viewed_on` DATETIME
);

# --- !Downs
DROP TABLE `jobs`;
