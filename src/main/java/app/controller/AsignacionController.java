package app.controller;

import app.model.dao.HorarioDAO;
import app.model.dao.VistaResultadoDAO;
import app.model.service.ExcelService;
import app.model.service.SimulatedAnnealingService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;

public class AsignacionController {

    @FXML private TextArea txtConsola;
    @FXML private Button btnAsignar;
    @FXML private ProgressIndicator progressIndicator;

    private final HorarioDAO horarioDAO = new HorarioDAO();
    private final SimulatedAnnealingService algoritmo = new SimulatedAnnealingService();
    private final ExcelService excelService = new ExcelService();
    private final VistaResultadoDAO vistaDAO = new VistaResultadoDAO();

    @FXML
    public void initialize() {
        escribirConsola("sistema listo. presione 'ejecutar algoritmo' para comenzar.");
    }

    @FXML
    public void limpiarAulas() {
        horarioDAO.limpiarAsignaciones();
        escribirConsola("todas las aulas han sido liberadas (estado null).");
    }

    @FXML
    public void ejecutarAlgoritmo() {
        btnAsignar.setDisable(true);
        progressIndicator.setVisible(true);
        txtConsola.clear();

        // creamos una tarea en segundo plano
        Task<Void> tareaAsignacion = new Task<>() {
            @Override
            protected Void call() {
                // pasamos un lambda para actualizar el textarea desde el hilo secundario
                algoritmo.ejecutarMejorDeTres(mensaje -> {
                    Platform.runLater(() -> escribirConsola(mensaje));
                });
                return null;
            }
        };

        // cuando termine la tarea
        tareaAsignacion.setOnSucceeded(e -> {
            btnAsignar.setDisable(false);
            progressIndicator.setVisible(false);
            escribirConsola("--- proceso completado. ya puede exportar o cerrar ---");
        });

        tareaAsignacion.setOnFailed(e -> {
            btnAsignar.setDisable(false);
            progressIndicator.setVisible(false);
            escribirConsola("error critico durante la ejecucion: " + tareaAsignacion.getException().getMessage());
        });

        // iniciamos el hilo
        new Thread(tareaAsignacion).start();
    }

    @FXML
    public void exportarExcel() {
        String path = System.getProperty("user.home") + "/Desktop/Reporte_Aulas_FIQA.xlsx";
        try {
            // pasamos todos los datos actuales de la bd al excel
            excelService.generarReporte(path, vistaDAO.listarResultados());
            escribirConsola("excel exportado correctamente a: " + path);
            // NUEVO: Alerta nativa de JavaFX
            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alerta.setTitle("Éxito");
            alerta.setHeaderText(null);
            alerta.setContentText("reporte generado exitosamente en el escritorio.");
            alerta.showAndWait();
        } catch (Exception e) {
            escribirConsola("error al exportar excel: " + e.getMessage());
        }
    }

    private void escribirConsola(String texto) {
        txtConsola.appendText(texto + "\n");
    }
}