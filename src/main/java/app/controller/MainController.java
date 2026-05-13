package app.controller;

import app.model.dao.VistaResultadoDAO;
import app.model.entity.ResultadoFinal;
import app.model.service.ExcelService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javax.swing.JOptionPane;
import java.io.IOException;

public class MainController {

    @FXML private TableView<ResultadoFinal> tablaResultados;
    @FXML private TableColumn<ResultadoFinal, String> colMateria;
    @FXML private TableColumn<ResultadoFinal, Integer> colSemestre;
    @FXML private TableColumn<ResultadoFinal, String> colParalelo;
    @FXML private TableColumn<ResultadoFinal, Integer> colCupos;
    @FXML private TableColumn<ResultadoFinal, String> colDocente;
    @FXML private TableColumn<ResultadoFinal, String> colAula;
    @FXML private TableColumn<ResultadoFinal, String> colTipoAula;
    @FXML private TableColumn<ResultadoFinal, String> colLunes;
    @FXML private TableColumn<ResultadoFinal, String> colMartes;
    @FXML private TableColumn<ResultadoFinal, String> colMiercoles;
    @FXML private TableColumn<ResultadoFinal, String> colJueves;
    @FXML private TableColumn<ResultadoFinal, String> colViernes;

    @FXML private TextField txtBuscarMateria;
    @FXML private TextField txtBuscarDocente;
    @FXML private Label lblEstado;

    private final VistaResultadoDAO vistaDAO = new VistaResultadoDAO();
    private final ExcelService excelService = new ExcelService();

    // listas para manejar el filtrado
    private ObservableList<ResultadoFinal> masterData = FXCollections.observableArrayList();
    private FilteredList<ResultadoFinal> filteredData;

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatos();
        configurarFiltros();
    }

    private void configurarColumnas() {
        // mapeo simple de solo lectura
        colMateria.setCellValueFactory(new PropertyValueFactory<>("materia"));
        colSemestre.setCellValueFactory(new PropertyValueFactory<>("semestre"));
        colParalelo.setCellValueFactory(new PropertyValueFactory<>("paralelo"));
        colCupos.setCellValueFactory(new PropertyValueFactory<>("numEstudiantes"));
        colDocente.setCellValueFactory(new PropertyValueFactory<>("profesor"));
        colAula.setCellValueFactory(new PropertyValueFactory<>("aulaNumero"));
        colTipoAula.setCellValueFactory(new PropertyValueFactory<>("tipoAula"));
        colLunes.setCellValueFactory(new PropertyValueFactory<>("lunes"));
        colMartes.setCellValueFactory(new PropertyValueFactory<>("martes"));
        colMiercoles.setCellValueFactory(new PropertyValueFactory<>("miercoles"));
        colJueves.setCellValueFactory(new PropertyValueFactory<>("jueves"));
        colViernes.setCellValueFactory(new PropertyValueFactory<>("viernes"));
    }

    @FXML
    public void cargarDatos() {
        masterData.setAll(vistaDAO.listarResultados());
        lblEstado.setText("registros cargados: " + masterData.size());
    }

    private void configurarFiltros() {
        filteredData = new FilteredList<>(masterData, p -> true);

        // listener para materia
        txtBuscarMateria.textProperty().addListener((observable, oldValue, newValue) -> actualizarPredicadoFiltro());

        // listener para docente
        txtBuscarDocente.textProperty().addListener((observable, oldValue, newValue) -> actualizarPredicadoFiltro());

        // conectar lista filtrada con la tabla y permitir ordenamiento por columnas
        SortedList<ResultadoFinal> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaResultados.comparatorProperty());
        tablaResultados.setItems(sortedData);
    }

    private void actualizarPredicadoFiltro() {
        String filtroMateria = txtBuscarMateria.getText().toLowerCase();
        String filtroDocente = txtBuscarDocente.getText().toLowerCase();

        filteredData.setPredicate(resultado -> {
            boolean coincideMateria = resultado.getMateria() != null && resultado.getMateria().toLowerCase().contains(filtroMateria);
            boolean coincideDocente = resultado.getProfesor() != null && resultado.getProfesor().toLowerCase().contains(filtroDocente);

            // si el campo de busqueda esta vacio, se considera como coincidencia
            if (filtroMateria.isEmpty()) coincideMateria = true;
            if (filtroDocente.isEmpty()) coincideDocente = true;

            return coincideMateria && coincideDocente;
        });

        lblEstado.setText("mostrando " + filteredData.size() + " registros.");
    }

    @FXML
    public void exportarExcel() {
        // usamos la lista filtrada que el usuario esta viendo en este momento
        String path = System.getProperty("user.home") + "/Desktop/Reporte_Aulas_FIQA.xlsx";
        try {
            excelService.generarReporte(path, masterData);
            lblEstado.setText("excel exportado a: " + path);
            // NUEVO: Alerta nativa de JavaFX
            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alerta.setTitle("Éxito");
            alerta.setHeaderText(null);
            alerta.setContentText("reporte generado exitosamente en el escritorio.");
            alerta.showAndWait();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "error al exportar excel: " + e.getMessage());
        }
    }

    @FXML
    public void irGestion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GestionView.fxml"));
            Parent nuevaVista = loader.load();

            // cambia la raiz de la escena actual en lugar de abrir una ventana nueva
            tablaResultados.getScene().setRoot(nuevaVista);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "error al cambiar de vista: " + e.getMessage());
        }
    }

    @FXML
    public void abrirAsignacion() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/AsignacionView.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("asignacion de aulas");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL); // bloquea la ventana principal

            // recargar datos en la tabla principal cuando se cierre el modal
            stage.setOnHidden(e -> cargarDatos());

            stage.showAndWait();
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(null, "error al abrir ventana de asignacion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}