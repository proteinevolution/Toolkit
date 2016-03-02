# --- !Ups

CREATE TABLE `jobs` (

	`main_id`             INT(11)       NOT NULL AUTO_INCREMENT PRIMARY KEY,
	`type`                VARCHAR(50),
	`parent_id`           INT(11),
	`job_id`              VARCHAR(100),
	`user_id`             VARCHAR(15),
	`status`              CHAR(1),
	`tool`                VARCHAR(100),
	`stat_id`             INT(11),
	`created_on`          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
	`updated_on`          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`viewed_on`           TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE `users` (

  `user_id`             INT(11)       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_name_first`     VARCHAR(50),
  `user_name_last`      VARCHAR(50),
  `password`            VARCHAR(50),
  `email`               VARCHAR(50),
	`created_on`          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
	`updated_on`          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE `user_job` (

  `users_user_id`       INT(11)       NOT NULL,
  `jobs_job_id`         INT(11)       NOT NULL,
  `main_user`           INT(1)
);


CREATE TABLE `sessions` (

  `session_id`          VARCHAR(15)   NOT NULL PRIMARY KEY,
  `users_user_id`       INT(11),
  `session_ip`          VARCHAR(40),
  `created_on`          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
  `updated_on`          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP
);


CREATE TABLE `session_job` (

  `sessions_session_id` VARCHAR(15)   NOT NULL,
  `jobs_job_id`         VARCHAR(100)  NOT NULL
);


# --- !Downs
DROP TABLE `jobs`;

DROP TABLE `users`;

DROP TABLE `user_job`;

DROP TABLE `sessions`;

DROP TABLE `session_job`;