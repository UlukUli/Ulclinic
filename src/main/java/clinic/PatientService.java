package clinic;

import java.sql.*;

public class PatientService {

    // История болезни
    public static void showHistory(String login) {
        String sql = "SELECT date, diagnosis FROM medical_history WHERE patient_login = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nИстория болезни:");
            boolean found = false;

            while (rs.next()) {
                System.out.println(rs.getString("date") + ": " + rs.getString("diagnosis"));
                found = true;
            }

            if (!found) {
                System.out.println("История болезни пуста.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    // Последняя дата диагноза
    public static void showLastDate(String login) {
        String sql = "SELECT date FROM medical_history WHERE patient_login = ? ORDER BY date DESC LIMIT 1";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Последняя дата болезни: " + rs.getString("date"));
            } else {
                System.out.println("История болезни пуста.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    // Количество дней лечения
    public static void showTreatmentDays(String login) {
        String sql = "SELECT treatment_days FROM patients WHERE login = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int days = rs.getInt("treatment_days");
                System.out.println("Дни лечения: " + days);
            } else {
                System.out.println("Пациент не найден.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    // Расписание врачей
    public static void showSchedule() {
        System.out.println("\n📅 Расписание врачей:");
        try (var reader = new java.io.BufferedReader(new java.io.FileReader("schedule.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка чтения расписания: " + e.getMessage());
        }
    }

    // Личная информация пациента
    public static void showPersonalInfo(String login) {
        String sql = "SELECT * FROM patients WHERE login = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n🧾 Информация о пациенте:");
                System.out.println("ФИО: " + rs.getString("fio"));
                System.out.println("Дата рождения: " + rs.getString("birth_date"));
                System.out.println("Рост: " + rs.getInt("height") + " см");
                System.out.println("Вес: " + rs.getInt("weight") + " кг");
                System.out.println("Группа крови: " + rs.getString("blood_type"));
            } else {
                System.out.println("Пациент не найден.");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }
}