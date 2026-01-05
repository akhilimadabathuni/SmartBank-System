# SmartBank System 🏦

A robust, console-based banking application built with **Java** and **MySQL**. 
This project demonstrates core backend development skills including **Object-Oriented Programming (OOP)**, **JDBC connectivity**, and **ACID-compliant transaction management**.

## 🚀 Key Features

* **OOP Architecture:** Uses Polymorphism to handle different account types (`SavingsAccount` vs. `CurrentAccount`) with distinct business rules.
* **Database Integration:** Connects to a cloud-based MySQL database (TiDB) using JDBC.
* **Transaction Management:** Manually handles `commit` and `rollback` to ensure financial data integrity (ACID properties).
* **Security:** Uses `PreparedStatement` to prevent SQL Injection attacks.
* **Design Patterns:** Implements the **Singleton Pattern** for efficient database connections.

## 🛠️ Tech Stack

* **Language:** Java 17+
* **Database:** MySQL (TiDB Cloud Serverless)
* **IDE:** VS Code

<img width="492" height="113" alt="image" src="https://github.com/user-attachments/assets/23e4042e-dc7f-4e00-ae49-9ab5e8ae2af2" />


## ▶️ How to Run

Since this project uses an external JDBC library, you must include the lib folder in the classpath.

## Compile:

**Bash**:
javac -cp ".;lib/*" SmartBankSystem.java


## Run:

**Bash**:
java -cp ".;lib/*" SmartBankSystem
