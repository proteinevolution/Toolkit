# --- !Ups
CREATE TABLE `users` (

  `user_id`             INTEGER UNSIGNED    NOT NULL AUTO_INCREMENT PRIMARY KEY,
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
  `security_token_exp`  DATETIME,
  `created_on`          DATETIME,
  `updated_on`          DATETIME,
  `logged_in_on`        DATETIME
);

# --- !Downs
DROP TABLE `users`;
