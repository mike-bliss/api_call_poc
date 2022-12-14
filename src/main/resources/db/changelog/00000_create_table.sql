--liquibase formatted sql
--changeset release:1
DROP TABLE IF EXISTS action_requests;
CREATE TABLE action_requests (
     `id` INT(11) NOT NULL AUTO_INCREMENT,
     `delegated_action_id` INT(11) NOT NULL,
     `cloud_provider` VARCHAR(255) NOT NULL,
     `resource_type` VARCHAR(255) NOT NULL,
     `customer_id` INT(11) NOT NULL,
     `organization_id` INT(11) NOT NULL,
     `user_id` INT(11) NOT NULL,
     `total_violator_count` INT(11) NOT NULL,
     `success_count` INT(11) NOT NULL DEFAULT 0,
     `failure_count` INT(11) NOT NULL DEFAULT 0,
     `status` ENUM('PENDING', 'SUCCEEDED', 'FAILED', 'SUCCEEDED_WITH_FAILURES') DEFAULT 'PENDING',
     `status_message` VARCHAR(255) NULL,
     PRIMARY KEY (`id`),
     CONSTRAINT UNIQUE KEY (`delegated_action_id`)
);

DROP TABLE IF EXISTS violations;
CREATE TABLE violations (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `request_id` INT(11) NOT NULL,
    `delegated_action_id` INT(11) NOT NULL,
    `resource_id` VARCHAR(255) NOT NULL,
    `account_name` VARCHAR(255) NOT NULL,
    `ch_account_id` INT(11) NOT NULL,
    `region` VARCHAR(255) NULL,
    `status` ENUM('PENDING', 'SUCCEEDED', 'FAILED', 'SUCCEEDED_WITH_FAILURES') DEFAULT 'PENDING',
    `status_message` VARCHAR(255) NULL,
    PRIMARY KEY (`id`)
--     KEY `fk_action_requests_id` (`request_id`),
--     CONSTRAINT `fk_action_requests_id` FOREIGN KEY (`request_id`) REFERENCES `action_requests` (`id`) ON DELETE CASCADE
);