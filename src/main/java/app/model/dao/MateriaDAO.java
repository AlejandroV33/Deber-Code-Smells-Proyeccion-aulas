package app.model.dao;

import app.model.entity.Materia;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class MateriaDAO extends BaseDAO {

    public List<Materia> listar() {
        List<Materia> lista = new ArrayList<>();
        String sql = "SELECT * FROM materias ORDER BY nombre";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Materia(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("departamento"),
                        rs.getInt("creditos"),
                        rs.getInt("horas"),
                        rs.getInt("semestre"),
                        rs.getInt("aula_requerida")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al listar materias: " + e.getMessage());
        }
        return lista;
    }

    public void insertar(Materia materia) {
        String sql = "INSERT INTO materias (codigo, nombre, departamento, creditos, horas, semestre, aula_requerida) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, materia.getCodigo());
            stmt.setString(2, materia.getNombre());
            stmt.setString(3, materia.getDepartamento());
            stmt.setInt(4, materia.getCreditos());
            stmt.setInt(5, materia.getHoras());
            stmt.setInt(6, materia.getSemestre());
            stmt.setInt(7, materia.getAulaRequerida());
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al insertar materia: " + e.getMessage());
        }
    }

    public void actualizar(Materia materia) {
        String sql = """
            UPDATE materias SET codigo=?, nombre=?, departamento=?, creditos=?, horas=?, semestre=?, aula_requerida=?
            WHERE id=?
            """;

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, materia.getCodigo());
            stmt.setString(2, materia.getNombre());
            stmt.setString(3, materia.getDepartamento());
            stmt.setInt(4, materia.getCreditos());
            stmt.setInt(5, materia.getHoras());
            stmt.setInt(6, materia.getSemestre());
            stmt.setInt(7, materia.getAulaRequerida());
            stmt.setInt(8, materia.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al actualizar materia: " + e.getMessage());
        }
    }

    public java.util.List<app.model.entity.MateriaFila> listarTabla() {
        java.util.List<app.model.entity.MateriaFila> lista = new java.util.ArrayList<>();
        String sql = "SELECT m.*, t.nombre as tipo_req FROM materias m LEFT JOIN tipos_aulas t ON m.aula_requerida = t.id ORDER BY m.nombre";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                app.model.entity.MateriaFila m = new app.model.entity.MateriaFila();
                m.setId(rs.getInt("id"));
                m.setCodigo(rs.getString("codigo")); // Añadido
                m.setNombre(rs.getString("nombre"));
                m.setSemestre(rs.getInt("semestre"));
                m.setDepartamento(rs.getString("departamento"));
                m.setCreditos(rs.getInt("creditos"));
                m.setHoras(rs.getInt("horas"));
                m.setIdTipoAulaReq(rs.getInt("aula_requerida"));
                m.setTipoAulaReq(rs.getString("tipo_req"));
                lista.add(m);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public void guardar(app.model.entity.MateriaFila m) {
        boolean nuevo = m.getId() == 0;
        // Ahora el SQL incluye TODOS los campos, incluyendo el código
        String sql = nuevo ? "INSERT INTO materias (codigo, nombre, semestre, departamento, creditos, horas, aula_requerida) VALUES (?,?,?,?,?,?,?)"
                : "UPDATE materias SET codigo=?, nombre=?, semestre=?, departamento=?, creditos=?, horas=?, aula_requerida=? WHERE id=?";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, m.getCodigo());
            stmt.setString(2, m.getNombre());
            stmt.setInt(3, m.getSemestre());
            stmt.setString(4, m.getDepartamento());
            stmt.setInt(5, m.getCreditos());
            stmt.setInt(6, m.getHoras());
            stmt.setInt(7, m.getIdTipoAulaReq());
            if (!nuevo) stmt.setInt(8, m.getId());
            stmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM materias WHERE id = ?";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) { stmt.setInt(1, id); stmt.executeUpdate(); } catch (Exception e) { e.printStackTrace(); }
    }

    public Integer buscarIdPorCodigoONombre(String codigo, String nombre) {
        String sql = "SELECT id FROM materias WHERE codigo = ? OR nombre = ? LIMIT 1";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, codigo); stmt.setString(2, nombre);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public int insertarMínimaRetornandoId(String codigo, String nombre, int semestre) {
        String sql = "INSERT INTO materias (codigo, nombre, departamento, creditos, horas, semestre, aula_requerida) VALUES (?, ?, 'S/D', 3, 6, ?, 1)";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, codigo); stmt.setString(2, nombre); stmt.setInt(3, semestre);
            stmt.executeUpdate();
            try (java.sql.ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }
}