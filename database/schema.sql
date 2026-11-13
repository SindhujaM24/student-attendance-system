-- Student Attendance Management System - MySQL schema
-- Run this file first (MySQL Workbench: File > Open SQL Script > Run).
-- WARNING: it drops and recreates the four tables, so any old data in them is lost.

CREATE DATABASE IF NOT EXISTS attendance_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE attendance_db;

DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS users;

-- Login accounts (1 admin + 1 account per student)
CREATE TABLE users (
    id        BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username  VARCHAR(100) NOT NULL UNIQUE,          -- admin username or student email
    password  VARCHAR(255) NOT NULL,                 -- BCrypt hash
    full_name VARCHAR(100) NOT NULL,
    role      VARCHAR(20)  NOT NULL,                 -- ADMIN or STUDENT
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'STUDENT'))
);

CREATE TABLE students (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    student_code  VARCHAR(20)  NOT NULL UNIQUE,      -- Student ID shown to users, e.g. ST001
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(15)  NOT NULL,
    department    VARCHAR(50)  NOT NULL,
    year_of_study INT          NOT NULL,
    section       VARCHAR(10)  NOT NULL,
    user_id       BIGINT       NOT NULL UNIQUE,      -- one student = one login account
    CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE subjects (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(20)  NOT NULL UNIQUE,       -- Subject ID, e.g. CS301
    name         VARCHAR(100) NOT NULL,
    department   VARCHAR(50)  NOT NULL,
    semester     INT          NOT NULL
);

-- One row = one student in one subject on one date
CREATE TABLE attendance (
    id              BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT      NOT NULL,
    subject_id      BIGINT      NOT NULL,
    attendance_date DATE        NOT NULL,
    status          VARCHAR(10) NOT NULL,            -- PRESENT or ABSENT
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_subject FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE CASCADE,
    CONSTRAINT uq_attendance_once UNIQUE (student_id, subject_id, attendance_date),
    CONSTRAINT chk_attendance_status CHECK (status IN ('PRESENT', 'ABSENT'))
);
