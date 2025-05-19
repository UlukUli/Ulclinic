package clinic;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() {
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {

            System.out.println("Используемая база данных: " + new File("clinic.db").getAbsolutePath());

// Таблица пользователей
            String createUsers = """
CREATE TABLE IF NOT EXISTS users (
login TEXT PRIMARY KEY,
password TEXT NOT NULL,
role TEXT NOT NULL,
name TEXT NOT NULL
);
""";

// Таблица пациентов
            String createPatientsTable = """
CREATE TABLE IF NOT EXISTS patients (
login TEXT PRIMARY KEY,
fio TEXT,
birth_date TEXT,
height INTEGER,
weight INTEGER,
blood_type TEXT,
treatment_days INTEGER
);
""";

// История болезней
            String createHistory = """
CREATE TABLE IF NOT EXISTS medical_history (
id INTEGER PRIMARY KEY AUTOINCREMENT,
patient_login TEXT,
date TEXT,
diagnosis TEXT,
FOREIGN KEY (patient_login) REFERENCES users(login)
);
""";

// Поручения медсестрам
            String createTasksTable = """
CREATE TABLE IF NOT EXISTS tasks (
id INTEGER PRIMARY KEY AUTOINCREMENT,
description TEXT,
doctor_login TEXT,
nurse_login TEXT,
status TEXT,
date_assigned TEXT,
date_done TEXT,
UNIQUE(description, doctor_login, nurse_login, date_assigned)
);
""";

// Процедуры
            String createProceduresTable = """
CREATE TABLE IF NOT EXISTS procedures (
id INTEGER PRIMARY KEY AUTOINCREMENT,
description TEXT,
patient_login TEXT,
UNIQUE(description, patient_login)
);
""";

// Сотрудники (главврач)
            String createStaffTable = """
CREATE TABLE IF NOT EXISTS staff (
id INTEGER PRIMARY KEY AUTOINCREMENT,
name TEXT UNIQUE,
role TEXT,
salary INTEGER,
hire_date TEXT
);
""";

// Выполняем создание всех таблиц
            stmt.execute(createUsers);
            stmt.execute(createPatientsTable);
            stmt.execute(createHistory);
            stmt.execute(createTasksTable);
            stmt.execute(createProceduresTable);
            stmt.execute(createStaffTable);

// ====== Тестовые данные (временно закомментированы) ======

stmt.execute("""
INSERT OR IGNORE INTO users (login, password, role, name) VALUES
('patient1', '1234', 'patient', 'Бекзат Пациент'),
('akzhol', '1234', 'patient', 'Акжол Алиев'),
('doc1', '1234', 'doctor', 'Айгуль К'),
('nurse1', '1234', 'nurse', 'Медсестра Наталья'),
('chief1', '1234', 'chief', 'Главврач Иванова');
""");

stmt.execute("""
INSERT OR IGNORE INTO patients (login, fio, birth_date, height, weight, blood_type, treatment_days)
VALUES
('patient1', 'Бекзат Пациент', '2000-01-01', 180, 75, 'A+', 5),
('akzhol', 'Акжол Алиев', '2005-01-01', 175, 68, 'O-', 0);
""");

stmt.execute("""
INSERT OR IGNORE INTO staff (name, role, salary, hire_date) VALUES
('Айгуль К', 'doctor', 350000, '2022-09-01'),
('Медсестра Наталья', 'nurse', 220000, '2023-01-15'),
('Главврач Иванова', 'chief', 500000, '2021-06-10');
""");

 stmt.execute("INSERT OR IGNORE INTO procedures (description, patient_login) VALUES ('Измерить давление', 'patient1')");
 stmt.execute("INSERT OR IGNORE INTO tasks (description, doctor_login, nurse_login, status, date_assigned) VALUES ('Поставить капельницу', 'doc1', 'nurse1', 'pending', '2025-05-14')");


            System.out.println("✅ База данных инициализирована.");

        } catch (Exception e) {
            System.out.println("❌ Ошибка инициализации БД: " + e.getMessage());
        }
    }
}

