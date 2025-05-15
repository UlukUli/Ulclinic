package clinic;

import java.sql.*;

public class ChiefDoctorService {

    // Показать медсестёр
    public static void showNurses() {
        String sql = "SELECT name, hire_date, salary FROM staff WHERE role = 'nurse'";
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n🩺 Список медсестёр:");
            boolean found = false;
            while (rs.next()) {
                System.out.println("- " + rs.getString("name") +
                        " | Принята: " + rs.getString("hire_date") +
                        " | Зарплата: " + rs.getInt("salary"));
                found = true;
            }
            if (!found) {
                System.out.println("Нет медсестёр в базе.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    // Показать врачей
    public static void showDoctors() {
        String sql = "SELECT name, hire_date, salary FROM staff WHERE role = 'doctor'";
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n👨‍⚕️ Список лечащих врачей:");
            boolean found = false;
            while (rs.next()) {
                System.out.println("- " + rs.getString("name") +
                        " | Принят: " + rs.getString("hire_date") +
                        " | Зарплата: " + rs.getInt("salary"));
                found = true;
            }
            if (!found) {
                System.out.println("Нет врачей в базе.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    // Количество пациентов
    public static void countPatients() {
        String sql = "SELECT COUNT(*) AS total FROM patients";
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("Общее количество пациентов: " + rs.getInt("total"));
            } else {
                System.out.println("Не удалось получить количество пациентов.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    // Максимальная зарплата
    public static void showMaxSalary() {
        String sql = "SELECT name, role, salary FROM staff ORDER BY salary DESC LIMIT 1";
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("💰 Максимальная зарплата: " + rs.getString("name") +
                        " (" + rs.getString("role") + ") — " + rs.getInt("salary"));
            } else {
                System.out.println("Нет данных о сотрудниках.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    // Минимальная зарплата
    public static void showMinSalary() {
        String sql = "SELECT name, role, salary FROM staff ORDER BY salary ASC LIMIT 1";
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("💵 Минимальная зарплата: " + rs.getString("name") +
                        " (" + rs.getString("role") + ") — " + rs.getInt("salary"));
            } else {
                System.out.println("Нет данных о сотрудниках.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }
}
