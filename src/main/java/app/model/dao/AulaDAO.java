package app.model.dao;

import app.model.entity.Aula;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class AulaDAO extends BaseDAO {

    public List<Aula> listar() {
        List<Aula> lista = new ArrayList<>();
        String sql = "SELECT * FROM aulas ORDER BY edificio, numero";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Aula a = new Aula();
                a.setId(rs.getInt("id"));
                a.setEdificio(rs.getString("edificio"));
                a.setPiso(rs.getString("piso"));
                a.setNumero(rs.getString("numero"));
                a.setCapacidad(rs.getInt("capacidad"));
                a.setEstado(rs.getString("estado"));
                a.setDisponibilidad(rs.getString("disponibilidad"));
                a.setIdTipoAula(rs.getInt("id_tipo_aula"));
                lista.add(a);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al listar aulas: " + e.getMessage());
        }
        return lista;
    }

    public void insertar(Aula aula) {
        String sql = """
            INSERT INTO aulas (edificio, piso, numero, capacidad, estado, disponibilidad, id_tipo_aula)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, aula.getEdificio());
            stmt.setString(2, aula.getPiso());
            stmt.setString(3, aula.getNumero());
            stmt.setInt(4, aula.getCapacidad());
            stmt.setString(5, aula.getEstado());
            stmt.setString(6, aula.getDisponibilidad());
            stmt.setInt(7, aula.getIdTipoAula());
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al insertar aula: " + e.getMessage());
        }
    }

    public void actualizar(Aula aula) {
        String sql = """
            UPDATE aulas SET edificio=?, piso=?, numero=?, capacidad=?, estado=?, disponibilidad=?, id_tipo_aula=?
            WHERE id=?
            """;

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, aula.getEdificio());
            stmt.setString(2, aula.getPiso());
            stmt.setString(3, aula.getNumero());
            stmt.setInt(4, aula.getCapacidad());
            stmt.setString(5, aula.getEstado());
            stmt.setString(6, aula.getDisponibilidad());
            stmt.setInt(7, aula.getIdTipoAula());
            stmt.setInt(8, aula.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al actualizar aula: " + e.getMessage());
        }
    }

    public java.util.List<app.model.entity.AulaFila> listarTabla() {
        java.util.List<app.model.entity.AulaFila> lista = new java.util.ArrayList<>();
        String sql = "SELECT a.*, t.nombre as tipo_aula FROM aulas a LEFT JOIN tipos_aulas t ON a.id_tipo_aula = t.id ORDER BY a.edificio, a.numero";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql); java.sql.ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                app.model.entity.AulaFila a = new app.model.entity.AulaFila();
                a.setId(rs.getInt("id")); a.setEdificio(rs.getString("edificio")); a.setPiso(rs.getString("piso"));
                a.setNumero(rs.getString("numero")); a.setCapacidad(rs.getInt("capacidad")); a.setEstado(rs.getString("estado"));
                a.setIdTipoAula(rs.getInt("id_tipo_aula")); a.setTipoAula(rs.getString("tipo_aula"));
                lista.add(a);
            }
        } catch (Exception e) { e.printStackTrace(); } return lista;
    }

    public void guardar(app.model.entity.AulaFila a) {
        boolean nuevo = a.getId() == 0;
        String sql = nuevo ? "INSERT INTO aulas (edificio, piso, numero, capacidad, estado, id_tipo_aula) VALUES (?,?,?,?,?,?)"
                : "UPDATE aulas SET edificio=?, piso=?, numero=?, capacidad=?, estado=?, id_tipo_aula=? WHERE id=?";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, a.getEdificio()); stmt.setString(2, a.getPiso()); stmt.setString(3, a.getNumero());
            stmt.setInt(4, a.getCapacidad()); stmt.setString(5, a.getEstado()); stmt.setInt(6, a.getIdTipoAula());
            if (!nuevo) stmt.setInt(7, a.getId());
            stmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM aulas WHERE id = ?";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) { stmt.setInt(1, id); stmt.executeUpdate(); } catch (Exception e) { e.printStackTrace(); }
    }

    public Integer buscarAulaInteligente(String textoExcel) {
        if (textoExcel == null || textoExcel.trim().isEmpty() || textoExcel.contains("SIN EDIFICIO") || textoExcel.contains("SE/")) return null;

        // Ej: E06/ P5/ E006 -> partimos por "/"
        String[] partes = textoExcel.split("/");
        if (partes.length < 3) return null;

        String edificio = partes[0].trim(); // "E06"
        String numero = partes[2].trim();   // "E006"

        String sql = "SELECT id FROM aulas WHERE edificio LIKE ? AND numero LIKE ? LIMIT 1";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + edificio + "%");
            stmt.setString(2, "%" + numero + "%");
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null; // Si no hay match, retorna null
    }
}