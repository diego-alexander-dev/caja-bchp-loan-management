# Caja BCHP - Student Loan Management System

A desktop financial management system designed to administer, process, and track student loans with transactional consistency and data integrity.

## Key Features
* **Loan Administration & Calculation:** Automated processing of loan schedules, interest rates, amortizations, and balance tracking.
* **Transactional Consistency:** Enforced business validation rules in Java to guarantee accurate financial balances and prevent calculation discrepancies.
* **Layered Architecture:** Clear decoupling between database access operations (DAO pattern), business service logic, and user interface.
* **Relational Persistence:** Normalized schema in PostgreSQL handling relational constraints across students, loans, and transaction histories.

## Tech Stack
* Java (JDK 17+)
* PostgreSQL
* JDBC
* Java Swing (GUI)
* Git

## Repository Structure
* `/src`: Java source code (domain models, DAO layer, services, and UI components).
* `/database`:
  * `schema.sql`: PostgreSQL DDL scripts (table structures, relations, foreign keys, and indexes).

## How to Set Up
1. **Database Setup:**
   * Create a database in PostgreSQL:
     ```sql
     CREATE DATABASE caja_bchp;
     ```
   * Run the SQL script located in `database/schema.sql`.
2. **Application Configuration:**
   * Clone the repository:
     ```bash
     git clone [https://github.com/diego-alexander-dev/caja-bchp-loan-management.git](https://github.com/diego-alexander-dev/caja-bchp-loan-management.git)
     ```
   * Configure your database credentials (`url`, `user`, `password`) in your connection properties class.
   * Open in IntelliJ IDEA / Eclipse / NetBeans and run the project.
