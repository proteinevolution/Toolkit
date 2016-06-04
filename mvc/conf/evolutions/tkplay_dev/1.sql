# --- !Ups
CREATE TABLE `users` (

  `user_id`      INTEGER UNSIGNED    NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name_login`   VARCHAR(80)         NOT NULL UNIQUE,
  `name_first`   VARCHAR(50),
  `name_last`    VARCHAR(50),
  `password`     VARCHAR(60)         NOT NULL,
  `email`        VARCHAR(100)        NOT NULL,
  `institute`    VARCHAR(100)        NULL,
  `street`       VARCHAR(100)        NULL,
  `city`         VARCHAR(100)        NULL,
  `country`      VARCHAR(100)        NULL,
  `groups`       VARCHAR(100)        DEFAULT 'member',
  `role`         VARCHAR(100)        NULL,
  `created_on`   DATETIME,
  `updated_on`   DATETIME,
  `logged_in_on` DATETIME
);

CREATE TABLE `userverification` (

  `user_id`      INTEGER UNSIGNED    NOT NULL PRIMARY KEY,
  `token`        CHAR(10)            NOT NULL UNIQUE,
  `token_type`   CHAR(1)             NOT NULL DEFAULT 'E',
  `token_exp`    DATETIME
);

# --- !Downs
DROP TABLE `users`;
DROP TABLE `userverification`;
