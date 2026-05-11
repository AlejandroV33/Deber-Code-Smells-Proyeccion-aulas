package app.model.dao;

import app.model.entity.Docente;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class DocenteDAO extends BaseDAO {

    public List<Docente> listar() {
        List<Docente> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, cualquier_pizarra FROM docentes ORDER BY nombre";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Docente(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("cualquier_pizarra")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al listar docentes: " + e.getMessage());
        }
        return lista;
    }

    public void insertar(Docente docente) {
        String sql = "INSERT INTO docentes (nombre, cualquier_pizarra) VALUES (?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, docente.getNombre());
            stmt.setString(2, docente.getCualquierPizarra());
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al insertar docente: " + e.getMessage());
        }
    }

    public void actualizar(Docente docente) {
        String sql = "UPDATE docentes SET nombre = ?, cualquier_pizarra = ? WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, docente.getNombre());
            stmt.setString(2, docente.getCualquierPizarra());
            stmt.setInt(3, docente.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al actualizar docente: " + e.getMessage());
        }
    }

    public void guardar(app.model.entity.Docente docente) {
        boolean esNuevo = docente.getId() == 0;
        String sql = esNuevo
                ? "INSERT INTO docentes (nombre, cualquier_pizarra) VALUES (?, ?)"
                : "UPDATE docentes SET nombre = ?, cualquier_pizarra = ? WHERE id = ?";

        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, docente.getNombre());
            stmt.setString(2, docente.getCualquierPizarra());

            if (!esNuevo) {
                stmt.setInt(3, docente.getId());
            }
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.err.println("Error al guardar docente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM docentes WHERE id = ?";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.err.println("Error al eliminar docente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void vaciarTabla() {
        String sql = "DELETE FROM docentes";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.executeUpdate();
            // Opcional para reiniciar IDs en SQLite:
            getConnection().prepareStatement("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'docentes'").executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public int insertarRetornandoId(app.model.entity.Docente d) {
        String sql = "INSERT INTO docentes (nombre, cualquier_pizarra) VALUES (?, ?)";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, d.getNombre());
            stmt.setString(2, d.getCualquierPizarra() != null ? d.getCualquierPizarra() : "si");
            stmt.executeUpdate();
            try (java.sql.ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }
}