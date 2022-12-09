--liquibase formatted sql
--changeset release:1
CREATE TABLE api_call_results (
     `id` INT(11) NOT NULL AUTO_INCREMENT,
     `account_name` VARCHAR(255) NOT NULL,
     `ch_account_id` VARCHAR(255) NULL,
     `resource_id` VARCHAR(255) NULL,
     `region` VARCHAR(255) NULL,
     `status` ENUM('PENDING', 'SUCCEEDED', 'FAILED', 'SUCCEEDED_WITH_FAILURES') DEFAULT 'PENDING',
     `success_message` VARCHAR(255) NULL,
     `failure_message` VARCHAR(255) NULL,
     PRIMARY KEY (`id`)
);