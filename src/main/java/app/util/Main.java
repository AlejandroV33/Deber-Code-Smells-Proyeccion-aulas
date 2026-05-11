package app.util;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection.init();
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("Base de datos creada y conectada correctamente.");
            } else {
                System.out.println("No se pudo conectar a la base de datos.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}