package app.model.dao;

import app.model.entity.TipoAula;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class TipoAulaDAO extends BaseDAO {

    public List<TipoAula> listar() {
        List<TipoAula> lista = new ArrayList<>();
        String sql = "SELECT id, nombre FROM tipos_aulas ORDER BY nombre";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new TipoAula(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al listar tipos de aula: " + e.getMessage());
        }
        return lista;
    }

    public void insertar(TipoAula tipo) {
        String sql = "INSERT INTO tipos_aulas (nombre) VALUES (?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, tipo.getNombre());
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al crear tipo aula: " + e.getMessage());
        }
    }

    public void actualizar(TipoAula tipo) {
        String sql = "UPDATE tipos_aulas SET nombre = ? WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, tipo.getNombre());
            stmt.setInt(2, tipo.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al actualizar tipo aula: " + e.getMessage());
        }
    }

    public void guardar(app.model.entity.TipoAula tipo) {
        boolean esNuevo = tipo.getId() == 0;
        String sql = esNuevo
                ? "INSERT INTO tipos_aulas (nombre) VALUES (?)"
                : "UPDATE tipos_aulas SET nombre = ? WHERE id = ?";

        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, tipo.getNombre());

            if (!esNuevo) {
                stmt.setInt(2, tipo.getId());
            }
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.err.println("Error al guardar tipo de aula: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM tipos_aulas WHERE id = ?";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.err.println("Error al eliminar tipo de aula: " + e.getMessage());
            e.printStackTrace();
        }
    }
}