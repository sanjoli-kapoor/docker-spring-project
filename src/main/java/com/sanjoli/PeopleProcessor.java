package com.sanjoli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//before running app run:
//brew install mysql; brew services start mysql;
//mysql -u root -> logging in with root
//check if project_people database is created
//CREATE USER 'springuser'@'localhost' IDENTIFIED BY 'springpass';
// CREATE DATABASE project_people;
// GRANT ALL PRIVILEGES ON project_people.* TO 'springuser'@'localhost';
// FLUSH PRIVILEGES;
// SELECT USER(), CURRENT_USER(), DATABASE();
//mysql -u springuser -p give password springpass

@SpringBootApplication
public class PeopleProcessor {
    public static void main(String[] args) {
        SpringApplication.run(PeopleProcessor.class, args);
    }
}
