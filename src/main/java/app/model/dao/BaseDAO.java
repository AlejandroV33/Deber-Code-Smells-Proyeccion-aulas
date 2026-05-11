package app.model.dao;

import app.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseDAO {

    protected Connection getConnection() {
        return DatabaseConnection.getConnection();
    }

    protected void close(PreparedStatement stmt) {
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) { /* ignorar */ }
        }
    }

    protected void close(ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { /* ignorar */ }
        }
    }
}