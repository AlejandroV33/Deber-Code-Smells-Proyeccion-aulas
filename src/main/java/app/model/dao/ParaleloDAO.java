package app.model.dao;

import app.model.entity.Paralelo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class ParaleloDAO extends BaseDAO {

    public List<Paralelo> listar() {
        List<Paralelo> lista = new ArrayList<>();
        String sql = "SELECT * FROM paralelos";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Paralelo(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("num_estudiantes_matriculados"),
                        rs.getInt("id_materia"),
                        rs.getInt("id_docente"),
                        rs.getString("espejo")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al listar paralelos: " + e.getMessage());
        }
        return lista;
    }

    public void insertar(Paralelo p) {
        String sql = """
            INSERT INTO paralelos (nombre, num_estudiantes_matriculados, id_materia, id_docente, espejo)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, p.getNombre());
            stmt.setInt(2, p.getNumEstudiantesMatriculados());
            stmt.setInt(3, p.getIdMateria());
            // manejo de nulos para docente
            if (p.getIdDocente() > 0) stmt.setInt(4, p.getIdDocente());
            else stmt.setNull(4, java.sql.Types.INTEGER);

            stmt.setString(5, p.getEspejo());
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al insertar paralelo: " + e.getMessage());
        }
    }

    public void actualizar(Paralelo p) {
        String sql = """
            UPDATE paralelos SET nombre=?, num_estudiantes_matriculados=?, id_materia=?, id_docente=?, espejo=?
            WHERE id=?
            """;

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, p.getNombre());
            stmt.setInt(2, p.getNumEstudiantesMatriculados());
            stmt.setInt(3, p.getIdMateria());

            if (p.getIdDocente() > 0) stmt.setInt(4, p.getIdDocente());
            else stmt.setNull(4, java.sql.Types.INTEGER);

            stmt.setString(5, p.getEspejo());
            stmt.setInt(6, p.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "error al actualizar paralelo: " + e.getMessage());
        }
    }

    public List<app.model.entity.ParaleloDetalle> listarDetalles() {
        List<app.model.entity.ParaleloDetalle> lista = new ArrayList<>();
        String sql = """
            SELECT p.id, m.nombre as materia, p.nombre as paralelo, d.nombre as docente
            FROM paralelos p
            JOIN materias m ON p.id_materia = m.id
            LEFT JOIN docentes d ON p.id_docente = d.id
            ORDER BY m.nombre, p.nombre
            """;
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new app.model.entity.ParaleloDetalle(
                        rs.getInt("id"), rs.getString("materia"),
                        rs.getString("paralelo"), rs.getString("docente")
                ));
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<app.model.entity.ParaleloFila> listarTabla() {
        List<app.model.entity.ParaleloFila> lista = new ArrayList<>();
        String sql = """
            SELECT p.id, p.nombre, p.num_estudiantes_matriculados,
                   m.id as id_materia, m.nombre as materia,
                   d.id as id_docente, d.nombre as docente
            FROM paralelos p
            JOIN materias m ON p.id_materia = m.id
            LEFT JOIN docentes d ON p.id_docente = d.id
            ORDER BY m.nombre, p.nombre
            """;
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                app.model.entity.ParaleloFila f = new app.model.entity.ParaleloFila();
                f.setId(rs.getInt("id"));
                f.setNombre(rs.getString("nombre"));
                f.setNumEstudiantes(rs.getInt("num_estudiantes_matriculados"));
                f.setIdMateria(rs.getInt("id_materia"));
                f.setMateria(rs.getString("materia"));
                int idDoc = rs.getInt("id_docente");
                if (!rs.wasNull()) {
                    f.setIdDocente(idDoc);
                    f.setDocente(rs.getString("docente"));
                } else {
                    f.setDocente("SIN DOCENTE");
                }
                lista.add(f);
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public void guardar(app.model.entity.ParaleloFila p) {
        boolean esNuevo = p.getId() == 0;
        String sql = esNuevo
                ? "INSERT INTO paralelos (nombre, num_estudiantes_matriculados, id_materia, id_docente) VALUES (?, ?, ?, ?)"
                : "UPDATE paralelos SET nombre=?, num_estudiantes_matriculados=?, id_materia=?, id_docente=? WHERE id=?";

        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, p.getNombre());
            stmt.setInt(2, p.getNumEstudiantes());
            stmt.setInt(3, p.getIdMateria());
            if (p.getIdDocente() == null) stmt.setNull(4, java.sql.Types.INTEGER);
            else stmt.setInt(4, p.getIdDocente());

            if (!esNuevo) stmt.setInt(5, p.getId());

            stmt.executeUpdate();
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM paralelos WHERE id = ?";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
    }

    public void vaciarTabla() {
        String sql = "DELETE FROM paralelos";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.executeUpdate();
            // Opcional para reiniciar IDs en SQLite:
            getConnection().prepareStatement("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'paralelos'").executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public int insertarRetornandoId(String nombre, int matriculados, int idMateria, int idDocente) {
        String sql = "INSERT INTO paralelos (nombre, num_estudiantes_matriculados, id_materia, id_docente) VALUES (?, ?, ?, ?)";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre); stmt.setInt(2, matriculados);
            stmt.setInt(3, idMateria); stmt.setInt(4, idDocente);
            stmt.executeUpdate();
            try (java.sql.ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }
}