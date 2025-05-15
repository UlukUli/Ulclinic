package clinic;

import java.sql.*;
import java.util.Scanner;

public class NurseService {

    // Показать все процедуры
    public static void showProcedures() {
        System.out.println("\n📋 Список процедур:");
        String sql = "SELECT * FROM procedures";

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean found = false;
            while (rs.next()) {
                System.out.println("- " + rs.getString("description") + " | Пациент: " + rs.getString("patient_login"));
                found = true;
            }
            if (!found) {
                System.out.println("Нет записей о процедурах.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка при получении процедур: " + e.getMessage());
        }
    }

    // Поиск пациента по имени
    public static void findPatient() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите имя или фамилию пациента: ");
        String name = scanner.nextLine();

        String sql = "SELECT * FROM users WHERE role = 'patient' AND name LIKE ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                System.out.println("Пациент: " + rs.getString("name") + " | Логин: " + rs.getString("login"));
                found = true;
            }
            if (!found) {
                System.out.println("Пациенты не найдены.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка поиска пациента: " + e.getMessage());
        }
    }

    // Показать все активные поручения
    public static void showTasks() {
        System.out.println("\n📋 Список поручений:");
        String sql = "SELECT * FROM tasks WHERE status = 'pending'";

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean found = false;
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + " | " + rs.getString("description"));
                found = true;
            }
            if (!found) {
                System.out.println("Нет активных поручений.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка при получении поручений: " + e.getMessage());
        }
    }

    // Отметить поручение как выполненное
    public static void completeTask() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ID поручения: ");

        int id;
        try {
            id = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Некорректный ID.");
            return;
        }

        String sql = "UPDATE tasks SET status = 'done', date_done = CURRENT_DATE WHERE id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Поручение выполнено.");
            } else {
                System.out.println("⚠ Поручение не найдено.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка при выполнении поручения: " + e.getMessage());
        }
    }

    // Показать завершённые поручения
    public static void showCompletedTasks() {
        System.out.println("\n✅ Завершённые поручения:");
        String sql = "SELECT * FROM tasks WHERE status = 'done'";

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean found = false;
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + " | Выполнено: " + rs.getString("date_done"));
                found = true;
            }
            if (!found) {
                System.out.println("Нет завершённых поручений.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка при получении завершённых поручений: " + e.getMessage());
        }
    }
}