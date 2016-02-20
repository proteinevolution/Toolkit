# --- !Ups
CREATE TABLE `jobs` (

	`main_id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`type` varchar(50),
	`parent_id` int(11),
	`job_id` varchar(100),
	`user_id` int(11),
	`status` char(1),
	`tool` varchar(100),
	`stat_id` int(11),
	`created_on` DATETIME,
	`updated_on` DATETIME,
	`viewed_on` DATETIME
);

# --- !Downs
DROP TABLE `jobs`;

