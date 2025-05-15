package clinic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class AuthService {

    // Метод авторизации пользователя
    public static String[] login() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Логин: ");
        String login = scanner.nextLine();
        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        String sql = "SELECT name, role FROM users WHERE login = ? AND password = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                String role = rs.getString("role");
                return new String[]{name, role, login};
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка при попытке входа: " + e.getMessage());
        }

        return null; // если логин или пароль неверные
    }
}
