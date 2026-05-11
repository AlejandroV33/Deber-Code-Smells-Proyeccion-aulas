package app.model.dao;

import app.model.entity.Horario;
import app.model.entity.HorarioFila;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

public class HorarioDAO extends BaseDAO {

    // metodo para resetear pruebas
    public void limpiarAsignaciones() {
        String sql = "UPDATE horarios SET id_aula = NULL, proporcion_ocupacion = NULL, indice_ocupacion = NULL, indice_ajuste_ocupacion = NULL";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println(">> bd: asignaciones reseteadas a null.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // actualiza aula y metricas de un horario
    public void actualizarAsignacion(int idHorario, Integer idAula, String prop, double idxOcup, double idxAjuste) {
        String sql = "UPDATE horarios SET id_aula=?, proporcion_ocupacion=?, indice_ocupacion=?, indice_ajuste_ocupacion=? WHERE id=?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            if (idAula == null) stmt.setNull(1, java.sql.Types.INTEGER);
            else stmt.setInt(1, idAula);

            stmt.setString(2, prop);
            stmt.setDouble(3, idxOcup);
            stmt.setDouble(4, idxAjuste);
            stmt.setInt(5, idHorario);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("error update horario " + idHorario + ": " + e.getMessage());
        }
    }

    // obtiene horarios con info extra para el algoritmo (un dto ligero o un mapa seria mejor, pero usaremos objetos)
    // necesitamos saber el tipo de aula requerida por la materia del horario
    public List<HorarioDTO> listarParaAlgoritmo() {
        List<HorarioDTO> lista = new ArrayList<>();
        String sql = """
            SELECT h.id, h.dia, h.hora_inicio, h.hora_fin, h.id_paralelo, 
                   p.num_estudiantes_matriculados, p.nombre as paralelo_nombre,
                   m.aula_requerida, m.semestre, m.nombre as materia_nombre,
                   ta.nombre as nombre_tipo, d.nombre as docente_nombre
            FROM horarios h
            JOIN paralelos p ON h.id_paralelo = p.id
            JOIN materias m ON p.id_materia = m.id
            JOIN tipos_aulas ta ON m.aula_requerida = ta.id
            LEFT JOIN docentes d ON p.id_docente = d.id
            """;

        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while(rs.next()) {
                HorarioDTO dto = new HorarioDTO();
                dto.id = rs.getInt("id");
                dto.dia = rs.getString("dia");
                dto.horaInicio = rs.getInt("hora_inicio");
                dto.horaFin = rs.getInt("hora_fin");
                dto.matriculados = rs.getInt("num_estudiantes_matriculados");
                dto.idTipoAulaReq = rs.getInt("aula_requerida");
                dto.nombreTipoAula = rs.getString("nombre_tipo");
                dto.semestre = rs.getInt("semestre");

                // info para debug
                dto.paralelo = rs.getString("paralelo_nombre");
                dto.materia = rs.getString("materia_nombre");
                dto.docente = rs.getString("docente_nombre") != null ? rs.getString("docente_nombre") : "SIN DOCENTE";

                lista.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<app.model.entity.HorarioFila> listarFilasEdicion() {
        Map<String, app.model.entity.HorarioFila> mapa = new HashMap<>();

        String sql = """
            SELECT h.id as id_horario, h.dia, h.hora_inicio, h.hora_fin, h.id_aula,
                   p.id as id_paralelo, p.nombre as paralelo, p.num_estudiantes_matriculados,
                   m.id as id_materia, m.nombre as materia,
                   d.nombre as docente,
                   a.edificio, a.piso, a.numero, a.capacidad,
                   ta.nombre as tipo_req
            FROM horarios h
            JOIN paralelos p ON h.id_paralelo = p.id
            JOIN materias m ON p.id_materia = m.id
            LEFT JOIN docentes d ON p.id_docente = d.id
            LEFT JOIN tipos_aulas ta ON m.aula_requerida = ta.id
            LEFT JOIN aulas a ON h.id_aula = a.id
            ORDER BY m.nombre, p.nombre
            """;

        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int idParalelo = rs.getInt("id_paralelo");
                int idAula = rs.getInt("id_aula");
                boolean sinAula = rs.wasNull();

                String key = idParalelo + "-" + (sinAula ? "NULL" : idAula);

                app.model.entity.HorarioFila fila = mapa.get(key);
                if (fila == null) {
                    fila = new app.model.entity.HorarioFila();
                    fila.setIdParalelo(idParalelo);
                    fila.setIdMateria(rs.getInt("id_materia"));
                    fila.setMateria(rs.getString("materia"));
                    fila.setDocente(rs.getString("docente")); // Nuevo
                    fila.setParalelo(rs.getString("paralelo"));
                    fila.setMatriculados(rs.getInt("num_estudiantes_matriculados"));
                    fila.setTipoAulaReq(rs.getString("tipo_req"));

                    if (!sinAula) {
                        fila.setIdAula(idAula);
                        fila.setAulaDesc(rs.getString("edificio") + "-" + rs.getString("piso") + "-" + rs.getString("numero"));
                        fila.setCapacidadAula(rs.getInt("capacidad"));
                    } else {
                        fila.setAulaDesc("SIN AULA");
                    }
                    mapa.put(key, fila);
                }

                app.model.entity.Horario horarioObj = new app.model.entity.Horario();
                horarioObj.setId(rs.getInt("id_horario"));
                horarioObj.setDia(rs.getString("dia"));
                horarioObj.setHoraInicio(rs.getInt("hora_inicio"));
                horarioObj.setHoraFin(rs.getInt("hora_fin"));
                horarioObj.setIdParalelo(idParalelo);
                horarioObj.setIdAula(sinAula ? null : idAula);

                String dia = rs.getString("dia") != null ? rs.getString("dia").toLowerCase() : "";
                switch (dia) {
                    case "lunes": fila.setHorarioLunes(horarioObj); break;
                    case "martes": fila.setHorarioMartes(horarioObj); break;
                    case "miercoles": fila.setHorarioMiercoles(horarioObj); break;
                    case "jueves": fila.setHorarioJueves(horarioObj); break;
                    case "viernes": fila.setHorarioViernes(horarioObj); break;
                    case "sabado": fila.setHorarioSabado(horarioObj); break;
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(mapa.values());
    }

    public void insertar(app.model.entity.Horario h) {
        String sql = "INSERT INTO horarios (dia, hora_inicio, hora_fin, id_paralelo, id_aula) VALUES (?, ?, ?, ?, ?)";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, h.getDia());
            stmt.setInt(2, h.getHoraInicio());
            stmt.setInt(3, h.getHoraFin());
            stmt.setInt(4, h.getIdParalelo());
            if (h.getIdAula() == null) stmt.setNull(5, java.sql.Types.INTEGER);
            else stmt.setInt(5, h.getIdAula());
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void actualizarHorasYAula(int idHorario, int horaInicio, int horaFin, Integer idAula) {
        String sql = "UPDATE horarios SET hora_inicio=?, hora_fin=?, id_aula=? WHERE id=?";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, horaInicio);
            stmt.setInt(2, horaFin);
            if (idAula == null) stmt.setNull(3, java.sql.Types.INTEGER);
            else stmt.setInt(3, idAula);
            stmt.setInt(4, idHorario);
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setContentText("error al actualizar: " + e.getMessage());
            alert.show();
        }
    }

    public void eliminar(int idHorario) {
        String sql = "DELETE FROM horarios WHERE id = ?";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idHorario);
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void vaciarTabla() {
        String sql = "DELETE FROM horarios";
        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.executeUpdate();
            // Opcional para reiniciar IDs en SQLite:
            getConnection().prepareStatement("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'horarios'").executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public String verificarChoqueAula(int idAula, String dia, int horaInicio, int horaFin, Integer idHorarioExcluido) {
        // Lógica de solapamiento de horas: (hora_inicio_existente < hora_fin_nueva) AND (hora_fin_existente > hora_inicio_nueva)
        String sql = """
            SELECT m.nombre as materia, p.nombre as paralelo, h.hora_inicio, h.hora_fin 
            FROM horarios h
            JOIN paralelos p ON h.id_paralelo = p.id
            JOIN materias m ON p.id_materia = m.id
            WHERE h.id_aula = ? AND h.dia = ? 
              AND (? IS NULL OR h.id != ?)
              AND (h.hora_inicio < ? AND h.hora_fin > ?)
            LIMIT 1
            """;

        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idAula);
            stmt.setString(2, dia);

            if (idHorarioExcluido == null) {
                stmt.setNull(3, java.sql.Types.INTEGER);
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(3, idHorarioExcluido);
                stmt.setInt(4, idHorarioExcluido);
            }

            stmt.setInt(5, horaFin);
            stmt.setInt(6, horaInicio);

            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return "Materia: " + rs.getString("materia") + "\n" +
                            "Paralelo: " + rs.getString("paralelo") + "\n" +
                            "Horario Ocupado: " + rs.getInt("hora_inicio") + " a " + rs.getInt("hora_fin");
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return null; // Si retorna null, el aula está libre
    }

    public java.util.List<app.model.entity.AulaOcupacion> listarOcupacionAulas() {
        java.util.Map<Integer, app.model.entity.AulaOcupacion> mapa = new java.util.HashMap<>();

        String sql = """
            SELECT a.id, a.edificio, a.piso, a.numero, a.capacidad, t.nombre as tipo_aula,
                   h.dia, h.hora_inicio, h.hora_fin, m.nombre as materia, p.nombre as paralelo, d.nombre as docente
            FROM aulas a
            LEFT JOIN tipos_aulas t ON a.id_tipo_aula = t.id
            LEFT JOIN horarios h ON a.id = h.id_aula
            LEFT JOIN paralelos p ON h.id_paralelo = p.id
            LEFT JOIN materias m ON p.id_materia = m.id
            LEFT JOIN docentes d ON p.id_docente = d.id
            WHERE a.estado = 'activo'
            ORDER BY a.edificio, a.piso, a.numero, h.hora_inicio
            """;

        try (java.sql.PreparedStatement stmt = getConnection().prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int idAula = rs.getInt("id");

                app.model.entity.AulaOcupacion aulaOc = mapa.get(idAula);
                if (aulaOc == null) {
                    aulaOc = new app.model.entity.AulaOcupacion();
                    aulaOc.setIdAula(idAula);
                    aulaOc.setEdificio(rs.getString("edificio"));
                    aulaOc.setPiso(rs.getString("piso"));
                    aulaOc.setNumero(rs.getString("numero"));
                    aulaOc.setCapacidad(rs.getInt("capacidad"));
                    aulaOc.setTipoAula(rs.getString("tipo_aula")); // Nuevo campo
                    mapa.put(idAula, aulaOc);
                }

                String dia = rs.getString("dia");
                if (dia != null) {
                    aulaOc.agregarClase(
                            dia,
                            rs.getInt("hora_inicio"),
                            rs.getInt("hora_fin"),
                            rs.getString("materia"),
                            rs.getString("paralelo"),
                            rs.getString("docente") // Pasamos el nombre del docente
                    );
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return new java.util.ArrayList<>(mapa.values());
    }

    // clase interna para transporte de datos
    public static class HorarioDTO {
        public int id;
        public String dia;
        public int horaInicio;
        public int horaFin;
        public int matriculados;
        public int idTipoAulaReq;
        public String nombreTipoAula;
        public int semestre;
        public Integer idAulaAsignada;
        public String paralelo;
        public String materia;
        public String docente;
    }
}