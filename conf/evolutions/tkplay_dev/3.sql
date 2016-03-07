# --- !Ups

CREATE TABLE `job_reference` (

  `session_id`          VARCHAR(15)         NOT NULL,
  `users_user_id`       INTEGER UNSIGNED,
  `jobs_main_id`        INTEGER UNSIGNED    NOT NULL,
  `referral_link`       VARCHAR(15)         NOT NULL,
  `description_id`      INTEGER UNSIGNED,
  `created_on`          TIMESTAMP           DEFAULT CURRENT_TIMESTAMP,
  `updated_on`          TIMESTAMP           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`viewed_on`           TIMESTAMP           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

# --- !Downs
DROP TABLE `job_reference`;