# Project database schema
# This is not necessarily needed.
DROP DATABASE SMPDB;
CREATE DATABASE SMPDB;
USE SMPDB;

# Table creation
# COMMENTs are not required
CREATE TABLE Identity 
        (idnum BIGINT AUTO_INCREMENT PRIMARY KEY,
         handle   VARCHAR(100) UNIQUE,
         pass     VARCHAR(100) NOT NULL,
         fullname VARCHAR(100) NOT NULL,
         location VARCHAR(100),
         email    VARCHAR(100) NOT NULL,
         bdate    DATE NOT NULL,
         joined   TIMESTAMP
        ) COMMENT="The Identity Table";

# CONSTRAINT names are variable in nature and just need to be unique across 
# all tables.  They can be declared with the attribute.
CREATE TABLE Story 
        (sidnum  BIGINT AUTO_INCREMENT PRIMARY KEY,
         idnum   BIGINT,
         chapter VARCHAR(100),
         url     VARCHAR(100),
         expires DATETIME,
         tstamp  TIMESTAMP,
         CONSTRAINT fk_id FOREIGN KEY(idnum) REFERENCES Identity(idnum)
        ) COMMENT="The Story Table";

CREATE TABLE Follows 
        (follower   BIGINT,
         followed   BIGINT,
         tstamp     TIMESTAMP,
	CONSTRAINT fk_follower FOREIGN KEY(follower) REFERENCES Identity(idnum),
	CONSTRAINT fk_followed FOREIGN KEY(followed) REFERENCES Identity(idnum)
        ) COMMENT="The Follows Table";

CREATE TABLE Reprint
        (rpnum      BIGINT AUTO_INCREMENT PRIMARY KEY,
         idnum      BIGINT,
         sidnum     BIGINT,
         likeit     BOOLEAN,
         newstory   BIGINT,
         tstamp  TIMESTAMP,
	CONSTRAINT fk_idnum FOREIGN KEY(idnum) REFERENCES Identity(idnum),
	CONSTRAINT fk_sidnum FOREIGN KEY(sidnum) REFERENCES Story(sidnum),
	CONSTRAINT fk_newstory FOREIGN KEY(newstory) REFERENCES Story(sidnum)
        ) COMMENT="The Reprint Table";

CREATE TABLE Block 
        (blknum     BIGINT AUTO_INCREMENT PRIMARY KEY,
         idnum      BIGINT,
         blocked    BIGINT,
         tstamp     TIMESTAMP,
	CONSTRAINT fk_xidnum  FOREIGN KEY(idnum)   REFERENCES Identity(idnum),
	CONSTRAINT fk_blocked FOREIGN KEY(blocked) REFERENCES Identity(idnum)
        ) COMMENT="The Block Table";

GRANT ALL ON SMPDB.* TO 'paul'@'localhost' IDENTIFIED BY 'jellydonuts!'; 
GRANT ALL on SMPDB.* TO 'paul'@'belgarath.cs.uky.edu' IDENTIFIED BY 'jellydonuts!'; 
GRANT ALL on SMPDB.* TO 'paul'@'paul.cs.uky.edu' IDENTIFIED BY 'jellydonuts!'; 
FLUSH privileges;
