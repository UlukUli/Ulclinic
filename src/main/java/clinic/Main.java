package clinic;

import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("🔃 Запуск приложения...");
        DatabaseInitializer.initialize();

// ----- Отладочные методы (можно включить при необходимости) -----
// deleteAllMedicalHistoryFor("patient1");
// debugShowAllTasks();
// deleteAllTasks();
// deleteAllProcedures();
// deleteAllStaff();

        String[] user = AuthService.login();

        if (user == null) {
            System.out.println("❌ Неверный логин или пароль.");
            return;
        }

        String name = user[0];
        String role = user[1];
        String login = user[2];

        System.out.println("✅ Добро пожаловать, " + name + " (" + role + ")");

        switch (role) {
            case "doctor" -> showDoctorMenu(name);
            case "nurse" -> showMedAssistantMenu(name);
            case "chief" -> showMainDoctorMenu(name);
            case "patient" -> showPatientMenu(login);
            default -> System.out.println("⚠ Неизвестная роль.");
        }
    }

    public static void showDoctorMenu(String name) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nЗдравствуйте, доктор " + name + "!");
            System.out.println("1. Просмотреть список пациентов");
            System.out.println("2. Показать количество пациентов");
            System.out.println("3. Показать список поручений для медсестер");
            System.out.println("4. Написать поручение для медсестер");
            System.out.println("5. Показать завершенные поручения");
            System.out.println("6. Поиск пациента");
            System.out.println("7. Дать диагноз пациенту");
            System.out.println("8. Выход");
            System.out.print("Ваш выбор: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> showAllPatients();
                case 2 -> showPatientCount();
                case 3 -> showActiveNurseTasks();
                case 4 -> assignTaskForAssistant();
                case 5 -> showCompletedNurseTasks();
                case 6 -> findPatient();
                case 7 -> addDiagnosis();
                case 8 -> {
                    System.out.println("Выход...");
                    return;
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }
    public static void showAllPatients() {
        String sql = "SELECT login, birth_date FROM patients";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n📋 Список пациентов:");
            boolean found = false;
            while (rs.next()) {
                System.out.println("- " + rs.getString("login") + " | Дата рождения: " + rs.getString("birth_date"));
                found = true;
            }
            if (!found) System.out.println("Пациентов не найдено.");

        } catch (Exception e) {
            System.out.println("❌ Ошибка при получении списка: " + e.getMessage());
        }
    }
    public static void addDiagnosis() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите логин пациента: ");
        String login = scanner.nextLine();

// Проверим, существует ли пациент
        String checkSql = "SELECT * FROM patients WHERE login = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, login);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                System.out.println("❌ Пациент с таким логином не найден.");
                return;
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка при проверке пациента: " + e.getMessage());
            return;
        }

        System.out.print("Введите диагноз: ");
        String diagnosis = scanner.nextLine();
        String date = java.time.LocalDate.now().toString();

        String description = null;
        int treatmentDays = 0;

// Чтение diagnos.txt
        try (Scanner fileScanner = new Scanner(new java.io.File("diagnos.txt"))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String name = parts[0].trim().toLowerCase();
                    String[] info = parts[1].split(";");
                    if (name.equals(diagnosis.trim().toLowerCase()) && info.length == 2) {
                        description = info[0].trim();
                        treatmentDays = Integer.parseInt(info[1].trim());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка чтения файла diagnos.txt: " + e.getMessage());
            return;
        }

        if (description == null) {
            System.out.println("⚠ Диагноз не найден в файле diagnos.txt.");
            return;
        }

// Запись в medical_history
        String insertSql = "INSERT INTO medical_history (patient_login, date, diagnosis) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            insertStmt.setString(1, login);
            insertStmt.setString(2, date);
            insertStmt.setString(3, diagnosis);
            insertStmt.executeUpdate();

// Обновление лечения
            PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE patients SET treatment_days = ? WHERE login = ?");
            updateStmt.setInt(1, treatmentDays);
            updateStmt.setString(2, login);
            updateStmt.executeUpdate();

            System.out.println("✅ Диагноз добавлен: " + diagnosis);
            System.out.println("🩺 Назначено лечение: " + description + " (" + treatmentDays + " дней)");

        } catch (Exception e) {
            System.out.println("❌ Ошибка при добавлении диагноза: " + e.getMessage());
        }
    }

    public static void findPatient() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ФИО пациента (или часть): ");
        String namePart = scanner.nextLine();

        String sql = """
SELECT users.name, patients.birth_date
FROM users
JOIN patients ON users.login = patients.login
WHERE users.name LIKE ?
""";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + namePart + "%");
            ResultSet rs = stmt.executeQuery();

            System.out.println("🔎 Результаты поиска:");
            boolean found = false;
            while (rs.next()) {
                System.out.println("- " + rs.getString("name") + " | " + rs.getString("birth_date"));
                found = true;
            }
            if (!found) {
                System.out.println("Пациенты не найдены.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка при поиске пациента: " + e.getMessage());
        }
    }
    public static void assignTaskForAssistant() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите описание поручения для медсестры: ");
        String description = scanner.nextLine();

        String nurseLogin = "nurse1"; // можно заменить на выбор из списка
        String doctorLogin = "doc1"; // или передавать текущего пользователя
        String date = java.time.LocalDate.now().toString();

        String sql = "INSERT INTO tasks (description, doctor_login, nurse_login, status, date_assigned) " +
                "VALUES (?, ?, ?, 'pending', ?)";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, description);
            stmt.setString(2, doctorLogin);
            stmt.setString(3, nurseLogin);
            stmt.setString(4, date);
            stmt.executeUpdate();

            System.out.println("✅ Поручение добавлено для nurse1");

        } catch (Exception e) {
            System.out.println("❌ Ошибка при добавлении поручения: " + e.getMessage());
        }
    }
    public static void showMedAssistantMenu(String name) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.println("\nЗдравствуйте, медсестра " + name + "!");
            System.out.println("1. Показать список процедур");
            System.out.println("2. Найти пациента");
            System.out.println("3. Показать список поручений");
            System.out.println("4. Выполнить поручение");
            System.out.println("5. Показать завершенные поручения");
            System.out.println("6. Выход");
            System.out.print("Ваш выбор: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Неверный ввод. Попробуйте ещё раз.");
                continue;
            }

            switch (choice) {
                case 1 -> NurseService.showProcedures();
                case 2 -> NurseService.findPatient();
                case 3 -> NurseService.showTasks();
                case 4 -> NurseService.completeTask();
                case 5 -> NurseService.showCompletedTasks();
                case 6 -> {
                    System.out.println("До свидания!");
                    return;
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }
    public static void showMainDoctorMenu(String name) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.println("\nЗдравствуйте, главврач " + name + "!");
            System.out.println("1. Показать список медсестёр");
            System.out.println("2. Показать список лечащих врачей");
            System.out.println("3. Показать количество пациентов");
            System.out.println("4. Сотрудник с максимальной зарплатой");
            System.out.println("5. Сотрудник с минимальной зарплатой");
            System.out.println("6. Выход");
            System.out.print("Ваш выбор: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Неверный ввод. Попробуйте ещё раз.");
                continue;
            }

            switch (choice) {
                case 1 -> ChiefDoctorService.showNurses();
                case 2 -> ChiefDoctorService.showDoctors();
                case 3 -> ChiefDoctorService.countPatients();
                case 4 -> ChiefDoctorService.showMaxSalary();
                case 5 -> ChiefDoctorService.showMinSalary();
                case 6 -> {
                    System.out.println("До свидания!");
                    return;
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }
    public static void showPatientMenu(String login) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        System.out.println("Вы вошли как: " + login);

        while (true) {
            System.out.println("\nЗдравствуйте, пациент!");
            System.out.println("1. Показать историю болезни");
            System.out.println("2. Показать последнюю дату болезни");
            System.out.println("3. Показать дни лечения");
            System.out.println("4. Показать расписание врачей");
            System.out.println("5. Показать мою информацию");
            System.out.println("6. Выход");
            System.out.print("Ваш выбор: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Неверный ввод.");
                continue;
            }

            switch (choice) {
                case 1 -> PatientService.showHistory(login);
                case 2 -> PatientService.showLastDate(login);
                case 3 -> PatientService.showTreatmentDays(login);
                case 4 -> PatientService.showSchedule();
                case 5 -> PatientService.showPersonalInfo(login);
                case 6 -> {
                    System.out.println("До свидания!");
                    return;
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }
    public static void debugShowAllMedicalHistory() {
        System.out.println("----- Содержимое medical_history -----");
        String sql = "SELECT * FROM medical_history";

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        " | Логин: " + rs.getString("patient_login") +
                        " | Дата: " + rs.getString("date") +
                        " | Диагноз: " + rs.getString("diagnosis"));
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }
    public static void debugShowAllTasks() {
        System.out.println("------ Список всех задач ------");
        String sql = "SELECT * FROM tasks";

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        " | Описание: " + rs.getString("description") +
                        " | Доктор: " + rs.getString("doctor_login") +
                        " | Медсестра: " + rs.getString("nurse_login") +
                        " | Статус: " + rs.getString("status"));
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }
    public static void deleteAllMedicalHistoryFor(String login) {
        String sql = "DELETE FROM medical_history WHERE patient_login = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            int rows = stmt.executeUpdate();
            System.out.println("✅ Удалено диагнозов: " + rows);

        } catch (Exception e) {
            System.out.println("❌ Ошибка при удалении истории: " + e.getMessage());
        }
    }
    public static void deleteAllTasks() {
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM tasks");
            System.out.println("✅ Все поручения удалены.");

        } catch (Exception e) {
            System.out.println("❌ Ошибка при удалении задач: " + e.getMessage());
        }
    }
    public static void deleteAllProcedures() {
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM procedures");
            System.out.println("✅ Все процедуры удалены.");

        } catch (Exception e) {
            System.out.println("❌ Ошибка при удалении процедур: " + e.getMessage());
        }
    }
    public static void showPatientCount() {
        String sql = "SELECT COUNT(*) AS count FROM patients";

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("Общее количество пациентов: " + rs.getInt("count"));
            } else {
                System.out.println("Не удалось получить данные.");
            }

        } catch (Exception e) {
            System.out.println("Ошибка при подсчёте пациентов: " + e.getMessage());
        }
    }

    public static void showActiveNurseTasks() {
        String sql = "SELECT * FROM tasks WHERE status = 'pending'";
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\nСписок активных поручений медсестрам:");
            boolean found = false;
            while (rs.next()) {
                System.out.println("- " + rs.getString("description") + " (для: " + rs.getString("nurse_login") + ")");
                found = true;
            }
            if (!found) {
                System.out.println("Нет активных поручений.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка при получении задач: " + e.getMessage());
        }
    }

    public static void showCompletedNurseTasks() {
        String sql = "SELECT * FROM tasks WHERE status = 'done'";
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\nЗавершённые поручения медсестёр:");
            boolean found = false;
            while (rs.next()) {
                System.out.println("- " + rs.getString("description") + " (выполнила: " + rs.getString("nurse_login") + ")");
                found = true;
            }
            if (!found) {
                System.out.println("Нет завершённых поручений.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка при получении завершённых задач: " + e.getMessage());
        }
    }
    public static void deleteAllStaff() {
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM staff");
            System.out.println("✅ Таблица staff очищена.");

        } catch (Exception e) {
            System.out.println("❌ Ошибка при очистке staff: " + e.getMessage());
        }
    }

}
