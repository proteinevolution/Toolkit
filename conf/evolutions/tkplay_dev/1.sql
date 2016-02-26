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

CREATE TABLE `users` (

  `user_id` int(11) NOT NULL PRIMARY KEY,
  `user_name_first` varchar(50),
  `user_name_last` varchar(50),
  `password` varchar(50),
  `email` varchar(50),
	`created_on` DATETIME,
	`updated_on` DATETIME
);


# --- !Downs
DROP TABLE `jobs`;

DROP TABLE `users`;

