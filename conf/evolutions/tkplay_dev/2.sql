# --- !Ups
CREATE TABLE `users` (

  `id`                  INTEGER UNSIGNED    NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name_login`          VARCHAR(80),
  `name_first`          VARCHAR(50),
  `name_last`           VARCHAR(50),
  `password`            VARCHAR(50),
  `email`               VARCHAR(50)         NOT NULL,
  `institute`           VARCHAR(100)        NULL,
  `street`              VARCHAR(100)        NULL,
  `city`                VARCHAR(100)        NULL,
  `country`             VARCHAR(100)        NULL,
  `groups`              VARCHAR(100)        DEFAULT 'member',
  `role`                VARCHAR(100)        NULL,
  `security_token`      CHAR(40)            DEFAULT NULL,
  `security_token_exp`  TIMESTAMP           DEFAULT CURRENT_TIMESTAMP,
	`created_on`          TIMESTAMP           DEFAULT CURRENT_TIMESTAMP,
	`updated_on`          TIMESTAMP           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`logged_in_on`        TIMESTAMP           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

# --- !Downs
DROP TABLE `users`;