package app.controller;

import app.model.entity.HorarioFila;
import app.model.entity.Horario;
import app.model.entity.Aula;
import app.model.entity.ParaleloDetalle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class GestionController {

    @FXML private TabPane mainTabPane;
    @FXML private TabPane tabPaneEdicion;
    @FXML private Tab tabHorarios, tabParalelos, tabMaterias;

    @FXML private TextField txtBuscarMateria, txtBuscarDocente;
    @FXML private TableView<HorarioFila> tablaHorarios;
    @FXML private TableColumn<HorarioFila, String> colMateria, colDocente, colParalelo, colAula, colTipoReq, colLunes, colMartes, colMiercoles, colJueves, colViernes, colSabado;
    @FXML private TableColumn<HorarioFila, Integer> colMatriculados, colCapacidad;

    // Añadir estas variables arriba
    @FXML private TableView<app.model.entity.ParaleloFila> tablaParalelos;
    @FXML private TableColumn<app.model.entity.ParaleloFila, String> colParaleloMat, colParaleloDoc, colParaleloNom;
    @FXML private TableColumn<app.model.entity.ParaleloFila, Integer> colParaleloEst;
    @FXML private TextField txtBuscarParaleloMat, txtBuscarParaleloDoc, txtBuscarParaleloNom;

    private final app.model.dao.HorarioDAO horarioDAO = new app.model.dao.HorarioDAO();
    private final app.model.dao.AulaDAO aulaDAO = new app.model.dao.AulaDAO();
    private final app.model.dao.ParaleloDAO paraleloDAO = new app.model.dao.ParaleloDAO();
    // Necesitas estos DAOs para los ComboBoxes (Asegúrate de tenerlos creados)
    private final app.model.dao.MateriaDAO materiaDAO = new app.model.dao.MateriaDAO();
    private final app.model.dao.DocenteDAO docenteDAO = new app.model.dao.DocenteDAO();

    private List<Aula> todasLasAulasCache;
    private List<ParaleloDetalle> todosParalelosCache;
    private ObservableList<HorarioFila> masterData = FXCollections.observableArrayList();
    private FilteredList<HorarioFila> filteredData;
    private ObservableList<app.model.entity.ParaleloFila> masterParalelos = FXCollections.observableArrayList();
    private FilteredList<app.model.entity.ParaleloFila> filteredParalelos;

    @FXML private Tab tabDocentes, tabAulas, tabTipos;

    // MATERIAS
    @FXML private TableColumn<app.model.entity.MateriaFila, String> colMatCodigo;
    @FXML private TextField txtBuscarMateriaNom;
    @FXML private TableView<app.model.entity.MateriaFila> tablaMaterias;
    @FXML private TableColumn<app.model.entity.MateriaFila, String> colMatNombre, colMatDepto, colMatTipo;
    @FXML private TableColumn<app.model.entity.MateriaFila, Integer> colMatSemestre, colMatCreditos, colMatHoras;
    private ObservableList<app.model.entity.MateriaFila> masterMaterias = FXCollections.observableArrayList();
    private FilteredList<app.model.entity.MateriaFila> filteredMaterias;

    // DOCENTES
    @FXML private TextField txtBuscarDocenteNom;
    @FXML private TableView<app.model.entity.Docente> tablaDocentes;
    @FXML private TableColumn<app.model.entity.Docente, String> colDocNombre, colDocPizarra;
    private ObservableList<app.model.entity.Docente> masterDocentes = FXCollections.observableArrayList();
    private FilteredList<app.model.entity.Docente> filteredDocentes;

    // AULAS
    @FXML private TextField txtBuscarAula;
    @FXML private TableView<app.model.entity.AulaFila> tablaAulas;
    @FXML private TableColumn<app.model.entity.AulaFila, String> colAulEdificio, colAulPiso, colAulNumero, colAulTipo, colAulEstado;
    @FXML private TableColumn<app.model.entity.AulaFila, Integer> colAulCapacidad;
    private ObservableList<app.model.entity.AulaFila> masterAulas = FXCollections.observableArrayList();
    private FilteredList<app.model.entity.AulaFila> filteredAulas;

    // TIPOS
    @FXML private TableView<app.model.entity.TipoAula> tablaTiposAulas;
    @FXML private TableColumn<app.model.entity.TipoAula, String> colTipNombre;
    private ObservableList<app.model.entity.TipoAula> masterTipos = FXCollections.observableArrayList();

    private final app.model.dao.TipoAulaDAO tipoAulaDAO = new app.model.dao.TipoAulaDAO();

    // PESTAÑA IMPORTAR
    @FXML private TextArea txtConsolaExcel;
    @FXML private ProgressIndicator progressExcel;
    private final app.model.service.ExcelPrepararService excelPrepararService = new app.model.service.ExcelPrepararService();

    // Extraccion desde un excel
    private final app.model.service.ExcelExtractorService extractorService = new app.model.service.ExcelExtractorService();

    // --- VARIABLES DEL VISOR DE AULAS ---
    @FXML private ComboBox<String> comboFiltroEdificio, comboOrdenOcupacion;
    @FXML private TableView<app.model.entity.AulaOcupacion> tablaVisorAulas;
    @FXML private TableColumn<app.model.entity.AulaOcupacion, String> colVisAula, colVisLun, colVisMar, colVisMie, colVisJue, colVisVie;
    @FXML private CheckBox chkSoloOcupadas;

    private javafx.collections.ObservableList<app.model.entity.AulaOcupacion> masterOcupacion = javafx.collections.FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        todasLasAulasCache = aulaDAO.listar();
        todosParalelosCache = paraleloDAO.listarDetalles();

        configurarTablaHorarios();
        cargarDatosTabla();
        configurarFiltros();
        configurarTablaParalelos();
        configurarTablaMaterias();
        configurarTablaDocentes();
        configurarTablaAulas();
        configurarTablaTipos();
        configurarVisorAulas();
    }

    private void configurarTablaHorarios() {
        colMateria.setCellValueFactory(new PropertyValueFactory<>("materia"));
        colDocente.setCellValueFactory(new PropertyValueFactory<>("docente"));
        colParalelo.setCellValueFactory(new PropertyValueFactory<>("paralelo"));
        colMatriculados.setCellValueFactory(new PropertyValueFactory<>("matriculados"));
        colAula.setCellValueFactory(new PropertyValueFactory<>("aulaDesc"));
        colTipoReq.setCellValueFactory(new PropertyValueFactory<>("tipoAulaReq"));
        colCapacidad.setCellValueFactory(new PropertyValueFactory<>("capacidadAula"));

        colLunes.setCellValueFactory(new PropertyValueFactory<>("lunesText"));
        colMartes.setCellValueFactory(new PropertyValueFactory<>("martesText"));
        colMiercoles.setCellValueFactory(new PropertyValueFactory<>("miercolesText"));
        colJueves.setCellValueFactory(new PropertyValueFactory<>("juevesText"));
        colViernes.setCellValueFactory(new PropertyValueFactory<>("viernesText"));
        colSabado.setCellValueFactory(new PropertyValueFactory<>("sabadoText"));

        // Deep linking sutil (solo manito, sin azul)
        configurarDeepLink(colMateria, tabMaterias);
        configurarDeepLink(colParalelo, tabParalelos);
        configurarDeepLink(colMatriculados, tabParalelos);
        configurarDeepLink(colDocente, tabParalelos);

        // Edicion de horas
        configurarEdicionDia(colLunes, "lunes");
        configurarEdicionDia(colMartes, "martes");
        configurarEdicionDia(colMiercoles, "miercoles");
        configurarEdicionDia(colJueves, "jueves");
        configurarEdicionDia(colViernes, "viernes");
        configurarEdicionDia(colSabado, "sabado");
    }

    private void cargarDatosTabla() {
        masterData.setAll(horarioDAO.listarFilasEdicion());
    }

    private void configurarFiltros() {
        filteredData = new FilteredList<>(masterData, p -> true);
        txtBuscarMateria.textProperty().addListener((obs, oldV, newV) -> actualizarPredicado());
        txtBuscarDocente.textProperty().addListener((obs, oldV, newV) -> actualizarPredicado());

        SortedList<HorarioFila> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaHorarios.comparatorProperty());
        tablaHorarios.setItems(sortedData);
    }

    private void actualizarPredicado() {
        String filtroMat = txtBuscarMateria.getText().toLowerCase();
        String filtroDoc = txtBuscarDocente.getText().toLowerCase();

        filteredData.setPredicate(fila -> {
            boolean mat = fila.getMateria() != null && fila.getMateria().toLowerCase().contains(filtroMat);
            boolean doc = fila.getDocente() != null && fila.getDocente().toLowerCase().contains(filtroDoc);
            return (filtroMat.isEmpty() || mat) && (filtroDoc.isEmpty() || doc);
        });
    }

    // Estilo sutil de Deep Linking
    private <T> void configurarDeepLink(TableColumn<HorarioFila, T> columna, Tab destino) {
        columna.setCellFactory(tc -> {
            TableCell<HorarioFila, T> cell = new TableCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.toString());
                    setStyle(empty || item == null ? "" : "-fx-cursor: hand;");
                }
            };
            cell.setOnMouseClicked(e -> {
                if (!cell.isEmpty() && e.getClickCount() == 2) {
                    tabPaneEdicion.getSelectionModel().select(destino);
                }
            });
            return cell;
        });
    }

    private void configurarEdicionDia(TableColumn<HorarioFila, String> columna, String dia) {
        columna.setCellFactory(tc -> {
            TableCell<HorarioFila, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item);
                    setStyle(empty || item == null || item.isEmpty() ? "" : "-fx-cursor: hand; -fx-background-color: #e8f4f8;");
                }
            };
            cell.setOnMouseClicked(e -> {
                if (!cell.isEmpty() && e.getClickCount() == 2 && cell.getText() != null && !cell.getText().isEmpty()) {
                    abrirEditorHorario(cell.getTableView().getItems().get(cell.getIndex()), dia);
                }
            });
            return cell;
        });
    }

    // ---- MODAL NUEVO HORARIO ----
    @FXML
    public void abrirModalNuevoHorario() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Registrar Nuevo Horario");
        dialog.setHeaderText("Asigna un nuevo bloque de clases");

        // 1. Buscador Paralelo
        TextField txtFiltroParalelo = new TextField();
        txtFiltroParalelo.setPromptText("Filtrar materia o docente...");
        ComboBox<ParaleloDetalle> comboParalelos = new ComboBox<>();
        comboParalelos.setPrefWidth(300);
        llenarComboParalelos(comboParalelos, "");
        txtFiltroParalelo.textProperty().addListener((obs, oldV, newV) -> llenarComboParalelos(comboParalelos, newV.toLowerCase()));

        // 2. Dia y Horas
        ComboBox<String> comboDia = new ComboBox<>(FXCollections.observableArrayList("Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado"));
        comboDia.getSelectionModel().selectFirst();
        Spinner<Integer> spinInicio = new Spinner<>(7, 22, 7, 1);
        Spinner<Integer> spinFin = new Spinner<>(7, 22, 9, 1);

        // 3. Buscador Aula (Opcional)
        TextField txtFiltroAula = new TextField();
        txtFiltroAula.setPromptText("Edificio, tipo, numero...");
        ComboBox<Aula> comboAulas = new ComboBox<>();
        comboAulas.setPrefWidth(300);
        llenarComboAulas(comboAulas, "");
        txtFiltroAula.textProperty().addListener((obs, oldV, newV) -> llenarComboAulas(comboAulas, newV.toLowerCase()));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("1. Buscar Paralelo:"), 0, 0); grid.add(txtFiltroParalelo, 1, 0);
        grid.add(new Label("Seleccionar:"), 0, 1); grid.add(comboParalelos, 1, 1);
        grid.add(new Label("2. Día:"), 0, 2); grid.add(comboDia, 1, 2);
        grid.add(new Label("Hora Inicio/Fin:"), 0, 3); grid.add(new HBox(5, spinInicio, new Label("a"), spinFin), 1, 3);
        grid.add(new Label("3. Buscar Aula (Opcional):"), 0, 4); grid.add(txtFiltroAula, 1, 4);
        grid.add(new Label("Seleccionar:"), 0, 5); grid.add(comboAulas, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // --- NUEVO: FILTRO PARA VALIDAR CHOQUE ANTES DE CERRAR ---
        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (comboAulas.getValue() != null) {
                String choque = horarioDAO.verificarChoqueAula(
                        comboAulas.getValue().getId(),
                        comboDia.getValue().toLowerCase(),
                        spinInicio.getValue(),
                        spinFin.getValue(),
                        null // null porque es un horario nuevo, no tiene ID todavía
                );

                if (choque != null) {
                    new Alert(Alert.AlertType.WARNING, "¡Choque de Aula Detectado!\nEsa aula ya está ocupada:\n\n" + choque).showAndWait();
                    event.consume(); // Esto BLOQUEA que la ventana se cierre
                }
            }
        });
        // ---------------------------------------------------------

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && comboParalelos.getValue() != null) {
                Horario h = new Horario();
                h.setIdParalelo(comboParalelos.getValue().getId());
                h.setDia(comboDia.getValue().toLowerCase());
                h.setHoraInicio(spinInicio.getValue());
                h.setHoraFin(spinFin.getValue());
                if (comboAulas.getValue() != null) h.setIdAula(comboAulas.getValue().getId());
                horarioDAO.insertar(h);
                return true;
            }
            return false;
        });

        dialog.showAndWait().ifPresent(guardado -> {
            if (guardado) cargarDatosTabla();
        });
    }

    // ---- MODAL EDITAR HORARIO (El que ya teniamos) ----
    private void abrirEditorHorario(HorarioFila fila, String dia) {
        Horario h = obtenerHorarioDeFila(fila, dia);
        if (h == null) return;

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Editar Horario - " + dia.toUpperCase());
        dialog.setHeaderText(fila.getMateria() + " | " + fila.getParalelo());

        Spinner<Integer> spinInicio = new Spinner<>(7, 22, h.getHoraInicio(), 1);
        Spinner<Integer> spinFin = new Spinner<>(7, 22, h.getHoraFin(), 1);

        TextField txtFiltroAula = new TextField();
        txtFiltroAula.setPromptText("Filtrar edificio, tipo...");
        ComboBox<Aula> comboAulas = new ComboBox<>();
        comboAulas.setPrefWidth(250);
        llenarComboAulas(comboAulas, "");
        txtFiltroAula.textProperty().addListener((obs, oldV, newV) -> llenarComboAulas(comboAulas, newV.toLowerCase()));

        if (h.getIdAula() != null) {
            todasLasAulasCache.stream().filter(a -> a.getId() == h.getIdAula()).findFirst().ifPresent(comboAulas.getSelectionModel()::select);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Inicio:"), 0, 0); grid.add(spinInicio, 1, 0);
        grid.add(new Label("Fin:"), 0, 1); grid.add(spinFin, 1, 1);
        grid.add(new Label("Buscar:"), 0, 2); grid.add(txtFiltroAula, 1, 2);
        grid.add(new Label("Aula:"), 0, 3); grid.add(comboAulas, 1, 3);

        Button btnEliminar = new Button("🗑 Eliminar este Horario");
        btnEliminar.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        btnEliminar.setOnAction(e -> {
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "¿Seguro que desea eliminar el horario de " + dia + "?", ButtonType.YES, ButtonType.NO);
            conf.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    horarioDAO.eliminar(h.getId());
                    dialog.setResult(true); // Engaña al dialog para que se cierre y recargue
                    dialog.close();
                }
            });
        });
        grid.add(btnEliminar, 0, 4, 2, 1); // Lo añadimos al final del GridPane

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // --- NUEVO: FILTRO PARA VALIDAR CHOQUE ANTES DE CERRAR ---
        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (comboAulas.getValue() != null) {
                String choque = horarioDAO.verificarChoqueAula(
                        comboAulas.getValue().getId(),
                        dia.toLowerCase(),
                        spinInicio.getValue(),
                        spinFin.getValue(),
                        h.getId() // Le pasamos su propio ID para que no "choque consigo mismo"
                );

                if (choque != null) {
                    new Alert(Alert.AlertType.WARNING, "¡Choque de Aula Detectado!\nEsa aula ya está ocupada:\n\n" + choque).showAndWait();
                    event.consume(); // Esto BLOQUEA que la ventana se cierre
                }
            }
        });
        // ---------------------------------------------------------

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Integer idAula = comboAulas.getValue() != null ? comboAulas.getValue().getId() : null;
                horarioDAO.actualizarHorasYAula(h.getId(), spinInicio.getValue(), spinFin.getValue(), idAula);
                return true;
            }
            return false;
        });

        dialog.showAndWait().ifPresent(guardado -> {
            if (guardado) cargarDatosTabla();
        });
    }

    // Utilidades para los combobox
    private void llenarComboAulas(ComboBox<app.model.entity.Aula> combo, String filtro) {
        String filtroLimpio = filtro.trim().toUpperCase();

        List<app.model.entity.Aula> filtradas = new java.util.ArrayList<>();

        // 1. Agregamos un null real para que el código anterior siga funcionando intacto
        filtradas.add(null);

        // 2. Filtramos el resto de aulas normalmente
        filtradas.addAll(todasLasAulasCache.stream()
                .filter(a -> a.getEstado().equalsIgnoreCase("activo"))
                .filter(a -> {
                    if (filtroLimpio.isEmpty()) return true;
                    String rutaAula = (a.getEdificio() + "/" + a.getPiso() + "/" + a.getNumero()).toUpperCase();
                    return rutaAula.contains(filtroLimpio);
                })
                .toList());

        combo.setItems(javafx.collections.FXCollections.observableArrayList(filtradas));

        // 3. Renderizador Visual: Le enseñamos al ComboBox cómo mostrar el 'null' y cómo agregar el '(L)'
        javafx.util.Callback<ListView<app.model.entity.Aula>, ListCell<app.model.entity.Aula>> cellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(app.model.entity.Aula item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("--- SIN AULA ---"); // Si el objeto es null, dibuja esto
                } else {
                    // Asumiendo que el ID 1 es el Aula "COMÚN", cualquier otro ID es especial/laboratorio
                    String sufijoLab = (item.getIdTipoAula() != 1) ? " (L)" : "";

                    // Formato: E17/P2/E004 (L) (40)
                    setText(item.getEdificio() + "/" + item.getPiso() + "/" + item.getNumero() + sufijoLab + " (" + item.getCapacidad() + ")");
                }
            }
        };

        // Aplicamos el renderizador a la lista desplegable y al botón principal seleccionado
        combo.setCellFactory(cellFactory);
        combo.setButtonCell(cellFactory.call(null));
    }

    private void llenarComboParalelos(ComboBox<ParaleloDetalle> combo, String filtro) {
        List<ParaleloDetalle> filtradas = todosParalelosCache.stream()
                .filter(p -> filtro.isEmpty() || p.getMateria().toLowerCase().contains(filtro) || (p.getDocente() != null && p.getDocente().toLowerCase().contains(filtro))).toList();
        combo.setItems(FXCollections.observableArrayList(filtradas));
    }

    private Horario obtenerHorarioDeFila(HorarioFila fila, String dia) {
        switch (dia) {
            case "lunes": return fila.getHorarioLunes();
            case "martes": return fila.getHorarioMartes();
            case "miercoles": return fila.getHorarioMiercoles();
            case "jueves": return fila.getHorarioJueves();
            case "viernes": return fila.getHorarioViernes();
            case "sabado": return fila.getHorarioSabado();
            default: return null;
        }
    }

    // Llama a esto desde tu método initialize()
    private void configurarTablaParalelos() {
        colParaleloMat.setCellValueFactory(new PropertyValueFactory<>("materia"));
        colParaleloDoc.setCellValueFactory(new PropertyValueFactory<>("docente"));
        colParaleloNom.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colParaleloEst.setCellValueFactory(new PropertyValueFactory<>("numEstudiantes"));

        // Filtros de Paralelo
        filteredParalelos = new FilteredList<>(masterParalelos, p -> true);
        txtBuscarParaleloMat.textProperty().addListener((o, old, n) -> actualizarFiltroParalelos());
        txtBuscarParaleloDoc.textProperty().addListener((o, old, n) -> actualizarFiltroParalelos());
        txtBuscarParaleloNom.textProperty().addListener((o, old, n) -> actualizarFiltroParalelos());
        tablaParalelos.setItems(new SortedList<>(filteredParalelos));

        // Doble clic para editar paralelo
        tablaParalelos.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tablaParalelos.getSelectionModel().getSelectedItem() != null) {
                abrirEditorParalelo(tablaParalelos.getSelectionModel().getSelectedItem());
            }
        });

        cargarDatosParalelos();
    }

    private void cargarDatosParalelos() {
        masterParalelos.setAll(paraleloDAO.listarTabla());
    }

    private void actualizarFiltroParalelos() {
        String fMat = txtBuscarParaleloMat.getText().toLowerCase();
        String fDoc = txtBuscarParaleloDoc.getText().toLowerCase();
        String fNom = txtBuscarParaleloNom.getText().toLowerCase();

        filteredParalelos.setPredicate(p -> {
            boolean m = p.getMateria() != null && p.getMateria().toLowerCase().contains(fMat);
            boolean d = p.getDocente() != null && p.getDocente().toLowerCase().contains(fDoc);
            boolean n = p.getNombre() != null && p.getNombre().toLowerCase().contains(fNom);
            return m && d && n;
        });
    }

    @FXML
    public void abrirModalNuevoParalelo() {
        abrirEditorParalelo(new app.model.entity.ParaleloFila()); // Objeto vacío
    }

    private void abrirEditorParalelo(app.model.entity.ParaleloFila p) {
        boolean esNuevo = p.getId() == 0;
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(esNuevo ? "Registrar Nuevo Paralelo" : "Editar Paralelo");
        dialog.setHeaderText("Gestión de Paralelo y Asignación de Docente");

        // Campos
        TextField txtNombre = new TextField(p.getNombre());
        txtNombre.setPromptText("Ej. GR1");
        Spinner<Integer> spinEst = new Spinner<>(1, 150, esNuevo ? 30 : p.getNumEstudiantes(), 1);

        // ComboBoxes (Asumimos que materiaDAO.listar() y docenteDAO.listar() existen)
        ComboBox<app.model.entity.Materia> comboMat = new ComboBox<>(FXCollections.observableArrayList(materiaDAO.listar()));
        ComboBox<app.model.entity.Docente> comboDoc = new ComboBox<>(FXCollections.observableArrayList(docenteDAO.listar()));
        comboMat.setPrefWidth(200); comboDoc.setPrefWidth(200);

        // Preseleccionar
        if (!esNuevo) {
            comboMat.getItems().stream().filter(m -> m.getId() == p.getIdMateria()).findFirst().ifPresent(comboMat.getSelectionModel()::select);
            if (p.getIdDocente() != null) {
                comboDoc.getItems().stream().filter(d -> d.getId() == p.getIdDocente()).findFirst().ifPresent(comboDoc.getSelectionModel()::select);
            }
        }

        // Botones de salto [ + ]
        Button btnNuevaMat = new Button("+");
        btnNuevaMat.setOnAction(e -> { dialog.close(); tabPaneEdicion.getSelectionModel().select(tabMaterias); }); // Salta a Materias

        Button btnNuevoDoc = new Button("+");
        // Nota: Como no tenemos tabDocentes, asumimos que saltará al de Materias/Docentes en el futuro. Ajusta el destino.
        btnNuevoDoc.setOnAction(e -> { dialog.close(); tabPaneEdicion.getSelectionModel().select(tabMaterias); });

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Materia:"), 0, 0); grid.add(new HBox(5, comboMat, btnNuevaMat), 1, 0);
        grid.add(new Label("Docente:"), 0, 1); grid.add(new HBox(5, comboDoc, btnNuevoDoc), 1, 1);
        grid.add(new Label("Paralelo (Nombre):"), 0, 2); grid.add(txtNombre, 1, 2);
        grid.add(new Label("Est. Matriculados:"), 0, 3); grid.add(spinEst, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && comboMat.getValue() != null && !txtNombre.getText().trim().isEmpty()) {
                p.setNombre(txtNombre.getText().trim().toUpperCase());
                p.setNumEstudiantes(spinEst.getValue());
                p.setIdMateria(comboMat.getValue().getId());
                p.setIdDocente(comboDoc.getValue() != null ? comboDoc.getValue().getId() : null);
                paraleloDAO.guardar(p);
                return true;
            }
            return false;
        });

        dialog.showAndWait().ifPresent(guardado -> {
            if (guardado) cargarDatosParalelos(); // Recargar tabla paralelos
        });
    }

    @FXML
    public void eliminarParalelo() {
        app.model.entity.ParaleloFila selec = tablaParalelos.getSelectionModel().getSelectedItem();
        if (selec == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione un paralelo de la tabla.").show();
            return;
        }

        // Advertencia en cascada estricta
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "ADVERTENCIA CRÍTICA \n\nEstá a punto de eliminar el paralelo '" + selec.getNombre() +
                        "' de la materia '" + selec.getMateria() + "'.\n\n" +
                        "Esto BORRARÁ EN CASCADA todos los horarios de clases asignados a este paralelo en la base de datos.\n" +
                        "¿Está completamente seguro?",
                ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                paraleloDAO.eliminar(selec.getId());
                cargarDatosParalelos(); // Recarga paralelos
                cargarDatosTabla();     // Recarga también la tabla de Horarios para reflejar el borrado
            }
        });
    }

    // ==========================================
    // CONFIGURACIONES Y LISTADOS
    // ==========================================
    private void configurarTablaMaterias() {
        colMatCodigo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("codigo"));
        colMatNombre.setCellValueFactory(new PropertyValueFactory<>("nombre")); colMatDepto.setCellValueFactory(new PropertyValueFactory<>("departamento"));
        colMatTipo.setCellValueFactory(new PropertyValueFactory<>("tipoAulaReq")); colMatSemestre.setCellValueFactory(new PropertyValueFactory<>("semestre"));
        colMatCreditos.setCellValueFactory(new PropertyValueFactory<>("creditos")); colMatHoras.setCellValueFactory(new PropertyValueFactory<>("horas"));

        filteredMaterias = new FilteredList<>(masterMaterias, p -> true);
        txtBuscarMateriaNom.textProperty().addListener((o, old, n) -> filteredMaterias.setPredicate(m -> n.isEmpty() || m.getNombre().toLowerCase().contains(n.toLowerCase())));
        tablaMaterias.setItems(new SortedList<>(filteredMaterias));
        tablaMaterias.setOnMouseClicked(e -> { if(e.getClickCount() == 2 && tablaMaterias.getSelectionModel().getSelectedItem() != null) abrirEditorMateria(tablaMaterias.getSelectionModel().getSelectedItem()); });
        cargarMaterias();
    }
    private void cargarMaterias() { masterMaterias.setAll(materiaDAO.listarTabla()); }

    private void configurarTablaDocentes() {
        colDocNombre.setCellValueFactory(new PropertyValueFactory<>("nombre")); colDocPizarra.setCellValueFactory(new PropertyValueFactory<>("cualquierPizarra"));
        filteredDocentes = new FilteredList<>(masterDocentes, p -> true);
        txtBuscarDocenteNom.textProperty().addListener((o, old, n) -> filteredDocentes.setPredicate(d -> n.isEmpty() || d.getNombre().toLowerCase().contains(n.toLowerCase())));
        tablaDocentes.setItems(new SortedList<>(filteredDocentes));
        tablaDocentes.setOnMouseClicked(e -> { if(e.getClickCount() == 2 && tablaDocentes.getSelectionModel().getSelectedItem() != null) abrirEditorDocente(tablaDocentes.getSelectionModel().getSelectedItem()); });
        cargarDocentes();
    }
    private void cargarDocentes() { masterDocentes.setAll(docenteDAO.listar()); }

    private void configurarTablaAulas() {
        colAulEdificio.setCellValueFactory(new PropertyValueFactory<>("edificio"));
        colAulPiso.setCellValueFactory(new PropertyValueFactory<>("piso"));
        colAulNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colAulCapacidad.setCellValueFactory(new PropertyValueFactory<>("capacidad"));
        colAulTipo.setCellValueFactory(new PropertyValueFactory<>("tipoAula"));
        colAulEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // 1. Enseñar a la columna "Piso" a ordenarse lógicamente (PB < M < P1 < P2)
        colAulPiso.setComparator((piso1, piso2) -> Integer.compare(obtenerValorPiso(piso1), obtenerValorPiso(piso2)));

        // 2. Buscador Progresivo (Ej. E17/PB/E004)
        filteredAulas = new FilteredList<>(masterAulas, p -> true);
        txtBuscarAula.textProperty().addListener((o, old, n) -> {
            String filtro = n.trim().toUpperCase();
            filteredAulas.setPredicate(a -> {
                if (filtro.isEmpty()) return true;
                String rutaAula = (a.getEdificio() + "/" + a.getPiso() + "/" + a.getNumero()).toUpperCase();
                return rutaAula.contains(filtro);
            });
        });

        // 3. Vincular el ordenamiento de las columnas dando clic en los encabezados
        SortedList<app.model.entity.AulaFila> sortedData = new SortedList<>(filteredAulas);
        sortedData.comparatorProperty().bind(tablaAulas.comparatorProperty());
        tablaAulas.setItems(sortedData);

        tablaAulas.setOnMouseClicked(e -> {
            if(e.getClickCount() == 2 && tablaAulas.getSelectionModel().getSelectedItem() != null) {
                abrirEditorAula(tablaAulas.getSelectionModel().getSelectedItem());
            }
        });

        cargarAulas();

        // 4. Forzar el orden visual por defecto al abrir la pestaña (Edificio -> Piso -> Número)
        tablaAulas.getSortOrder().addAll(colAulEdificio, colAulPiso, colAulNumero);
    }
    private void cargarAulas() {
        List<app.model.entity.AulaFila> lista = aulaDAO.listarTabla();

        // Ordenamiento por defecto: Edificio ascendente -> Piso ascendente -> Número
        lista.sort((a, b) -> {
            int cmpEdif = a.getEdificio().compareToIgnoreCase(b.getEdificio());
            if (cmpEdif != 0) return cmpEdif;

            int cmpPiso = Integer.compare(obtenerValorPiso(a.getPiso()), obtenerValorPiso(b.getPiso()));
            if (cmpPiso != 0) return cmpPiso;

            return a.getNumero().compareToIgnoreCase(b.getNumero());
        });

        masterAulas.setAll(lista);
    }

    private void configurarTablaTipos() {
        colTipNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tablaTiposAulas.setItems(masterTipos);
        tablaTiposAulas.setOnMouseClicked(e -> { if(e.getClickCount() == 2 && tablaTiposAulas.getSelectionModel().getSelectedItem() != null) abrirEditorTipo(tablaTiposAulas.getSelectionModel().getSelectedItem()); });
        cargarTipos();
    }
    private void cargarTipos() { masterTipos.setAll(tipoAulaDAO.listar()); }

    // ==========================================
    // MODALES (NUEVO / EDITAR)
    // ==========================================
    @FXML
    public void abrirModalNuevaMateria() { abrirEditorMateria(new app.model.entity.MateriaFila()); }

    /**
     * Abre el diálogo para crear o editar una materia.
     * Refactorizado con Extract Function para reducir complejidad cognitiva.
     * 
     * @param m la materia a editar (ID=0 para nueva materia)
     */
    private void abrirEditorMateria(app.model.entity.MateriaFila m) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(determinarTituloDialogoMateria(m));

        // 1. Crear todos los controles del formulario
        java.util.Map<String, Object> controles = crearYConfigurarControlesMateria(m);

        // 2. Construir el grid con los controles
        GridPane grid = construirGridFormularioMateria(controles);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 3. Configurar el resultado del diálogo
        configurarResultadoDialogoMateria(dialog, m, controles);

        dialog.showAndWait().ifPresent(guardoExito -> {
            if (guardoExito) {
                cargarMaterias(); // Refresca la tabla
            }
        });
    }

    /**
     * Determina si es nueva materia o edición basado en el ID.
     * Extracción de condicional: Decompose Conditional
     */
    private String determinarTituloDialogoMateria(app.model.entity.MateriaFila m) {
        return m.getId() == 0 ? "Nueva Materia" : "Editar Materia";
    }

    /**
     * Crea y configura todos los controles (TextFields, Spinners, ComboBox) del formulario.
     * Retorna un Map con claves: txtCodigo, txtNombre, txtDepto, spinCreditos, spinHoras, spinSemestre, comboTipo
     */
    private java.util.Map<String, Object> crearYConfigurarControlesMateria(app.model.entity.MateriaFila m) {
        java.util.Map<String, Object> controles = new java.util.HashMap<>();

        // Crear TextFields con valores iniciales
        TextField txtCodigo = new TextField(m.getCodigo() != null ? m.getCodigo() : "");
        TextField txtNombre = new TextField(m.getNombre() != null ? m.getNombre() : "");
        TextField txtDepto = new TextField(m.getDepartamento() != null ? m.getDepartamento() : "");

        // Crear Spinners de créditos, horas y semestre
        Spinner<Integer> spinCreditos = crearSpinnerConValorInicial(0, 100, m.getId() == 0 ? 0 : m.getCreditos());
        Spinner<Integer> spinHoras = crearSpinnerConValorInicial(0, 100, m.getId() == 0 ? 0 : m.getHoras());
        Spinner<Integer> spinSemestre = crearSpinnerConValorInicial(0, 20, m.getId() == 0 ? 0 : m.getSemestre());

        // Crear ComboBox de tipos de aula
        ComboBox<app.model.entity.TipoAula> comboTipo = new ComboBox<>(
            javafx.collections.FXCollections.observableArrayList(tipoAulaDAO.listar())
        );
        seleccionarTipoAulaDefault(comboTipo, m);

        // Guardar en el mapa
        controles.put("txtCodigo", txtCodigo);
        controles.put("txtNombre", txtNombre);
        controles.put("txtDepto", txtDepto);
        controles.put("spinCreditos", spinCreditos);
        controles.put("spinHoras", spinHoras);
        controles.put("spinSemestre", spinSemestre);
        controles.put("comboTipo", comboTipo);

        return controles;
    }

    /**
     * Crea un Spinner editable con listener que fuerza guardar el valor al perder foco.
     * Extracción de lógica repetitiva: Extract Function
     */
    private Spinner<Integer> crearSpinnerConValorInicial(int min, int max, int valor) {
        Spinner<Integer> spinner = new Spinner<>(min, max, valor, 1);
        spinner.setEditable(true);

        // Hack para forzar que JavaFX guarde el valor escrito a mano si no se presiona Enter
        spinner.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                try {
                    int valorEscrito = Integer.parseInt(spinner.getEditor().getText());
                    spinner.getValueFactory().setValue(valorEscrito);
                } catch (NumberFormatException e) {
                    // Silenciosamente restaurar el valor anterior si hay error
                }
            }
        });

        return spinner;
    }

    /**
     * Selecciona el tipo de aula por defecto basado en si es nueva o edición.
     * Extracción de condicional compleja: Decompose Conditional
     */
    private void seleccionarTipoAulaDefault(ComboBox<app.model.entity.TipoAula> comboTipo, app.model.entity.MateriaFila m) {
        if (esMateriaNueva(m)) {
            // Selección por defecto: COMÚN (id 1) si es nuevo
            comboTipo.getItems().stream()
                .filter(t -> t.getId() == 1)
                .findFirst()
                .ifPresent(comboTipo.getSelectionModel()::select);
        } else {
            // Para edición, seleccionar el tipo actual
            comboTipo.getItems().stream()
                .filter(t -> t.getId() == m.getIdTipoAulaReq())
                .findFirst()
                .ifPresent(comboTipo.getSelectionModel()::select);
        }
    }

    /**
     * Verifica si la materia es nueva (ID = 0).
     */
    private boolean esMateriaNueva(app.model.entity.MateriaFila m) {
        return m.getId() == 0;
    }

    /**
     * Construye el GridPane con todos los controles del formulario.
     * Extracción de lógica de UI: Extract Function
     */
    private GridPane construirGridFormularioMateria(java.util.Map<String, Object> controles) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtCodigo = (TextField) controles.get("txtCodigo");
        TextField txtNombre = (TextField) controles.get("txtNombre");
        TextField txtDepto = (TextField) controles.get("txtDepto");
        Spinner<Integer> spinCreditos = (Spinner<Integer>) controles.get("spinCreditos");
        Spinner<Integer> spinHoras = (Spinner<Integer>) controles.get("spinHoras");
        Spinner<Integer> spinSemestre = (Spinner<Integer>) controles.get("spinSemestre");
        ComboBox<app.model.entity.TipoAula> comboTipo = (ComboBox<app.model.entity.TipoAula>) controles.get("comboTipo");

        // Agregar filas al grid
        grid.add(new Label("Código:"), 0, 0);
        grid.add(txtCodigo, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(txtNombre, 1, 1);
        grid.add(new Label("Departamento:"), 0, 2);
        grid.add(txtDepto, 1, 2);
        grid.add(new Label("Créditos:"), 0, 3);
        grid.add(spinCreditos, 1, 3);
        grid.add(new Label("Horas:"), 0, 4);
        grid.add(spinHoras, 1, 4);
        grid.add(new Label("Semestre:"), 0, 5);
        grid.add(spinSemestre, 1, 5);
        grid.add(new Label("Aula Req.:"), 0, 6);
        grid.add(comboTipo, 1, 6);

        return grid;
    }

    /**
     * Configura el result converter y listener del diálogo de materia.
     * Extracción de lógica de resultado: Extract Function
     */
    private void configurarResultadoDialogoMateria(Dialog<Boolean> dialog, app.model.entity.MateriaFila m, 
                                                    java.util.Map<String, Object> controles) {
        dialog.setResultConverter(btn -> {
            if (esClickEnOK(btn) && validarDatosFormularioMateria(controles)) {
                guardarDatosMateria(m, controles);
                return true;
            }
            return false;
        });
    }

    /**
     * Valida que el botón presionado sea OK.
     * Extracción de condicional simple.
     */
    private boolean esClickEnOK(ButtonType btn) {
        return btn == ButtonType.OK;
    }

    /**
     * Valida que los datos requeridos del formulario no estén vacíos.
     * Extracción de validación compleja: Decompose Conditional
     */
    private boolean validarDatosFormularioMateria(java.util.Map<String, Object> controles) {
        TextField txtNombre = (TextField) controles.get("txtNombre");
        ComboBox<app.model.entity.TipoAula> comboTipo = (ComboBox<app.model.entity.TipoAula>) controles.get("comboTipo");

        return !txtNombre.getText().trim().isEmpty() && comboTipo.getValue() != null;
    }

    /**
     * Guarda los datos de la materia desde los controles del formulario.
     * Extracción de lógica de guardado: Extract Function
     */
    private void guardarDatosMateria(app.model.entity.MateriaFila m, java.util.Map<String, Object> controles) {
        TextField txtCodigo = (TextField) controles.get("txtCodigo");
        TextField txtNombre = (TextField) controles.get("txtNombre");
        TextField txtDepto = (TextField) controles.get("txtDepto");
        Spinner<Integer> spinCreditos = (Spinner<Integer>) controles.get("spinCreditos");
        Spinner<Integer> spinHoras = (Spinner<Integer>) controles.get("spinHoras");
        Spinner<Integer> spinSemestre = (Spinner<Integer>) controles.get("spinSemestre");
        ComboBox<app.model.entity.TipoAula> comboTipo = (ComboBox<app.model.entity.TipoAula>) controles.get("comboTipo");

        m.setCodigo(txtCodigo.getText().trim().toUpperCase());
        m.setNombre(txtNombre.getText().trim().toUpperCase());
        m.setDepartamento(txtDepto.getText().trim().toUpperCase());
        m.setCreditos(spinCreditos.getValue());
        m.setHoras(spinHoras.getValue());
        m.setSemestre(spinSemestre.getValue());
        m.setIdTipoAulaReq(comboTipo.getValue().getId());

        materiaDAO.guardar(m);
    }

    @FXML public void abrirModalNuevoDocente() { abrirEditorDocente(new app.model.entity.Docente()); }
    private void abrirEditorDocente(app.model.entity.Docente d) {
        Dialog<Boolean> dialog = new Dialog<>(); dialog.setTitle(d.getId() == 0 ? "Nuevo Docente" : "Editar Docente");
        TextField txtNombre = new TextField(d.getNombre());
        ComboBox<String> comboPiz = new ComboBox<>(FXCollections.observableArrayList("si", "no"));
        comboPiz.setValue(d.getId() == 0 ? "si" : d.getCualquierPizarra());
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Nombre Completo:"), 0, 0); grid.add(txtNombre, 1, 0);
        grid.add(new Label("¿Cualquier Pizarra?:"), 0, 1); grid.add(comboPiz, 1, 1);
        dialog.getDialogPane().setContent(grid); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !txtNombre.getText().isEmpty()) {
                d.setNombre(txtNombre.getText()); d.setCualquierPizarra(comboPiz.getValue()); docenteDAO.guardar(d); return true;
            } return false;
        });
        dialog.showAndWait().ifPresent(res -> { if(res) cargarDocentes(); });
    }

    @FXML
    public void abrirModalNuevaAula() { abrirEditorAula(new app.model.entity.AulaFila()); }

    private void abrirEditorAula(app.model.entity.AulaFila a) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(a.getId() == 0 ? "Nueva Aula" : "Editar Aula");

        TextField txtEdif = new TextField(a.getEdificio());
        TextField txtPiso = new TextField(a.getPiso());
        TextField txtNum = new TextField(a.getNumero());

        // CORRECCIÓN DEL SPINNER: Salto de 1 en 1, y habilitar escritura manual
        Spinner<Integer> spinCap = new Spinner<>(1, 200, a.getId() == 0 ? 30 : a.getCapacidad(), 1);
        spinCap.setEditable(true); // Permite teclear el número directamente

        // Hack de JavaFX para asegurar que si tecleas y no das "Enter", igual guarde el valor al cambiar de celda
        spinCap.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                try {
                    spinCap.getValueFactory().setValue(Integer.parseInt(spinCap.getEditor().getText()));
                } catch (NumberFormatException e) {
                    spinCap.getEditor().setText(spinCap.getValue().toString()); // Restaurar si escribe letras
                }
            }
        });

        ComboBox<String> comboEst = new ComboBox<>(javafx.collections.FXCollections.observableArrayList("activo", "inactivo"));
        comboEst.setValue(a.getId() == 0 ? "activo" : a.getEstado());

        ComboBox<app.model.entity.TipoAula> comboTipo = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(tipoAulaDAO.listar()));
        if (a.getId() != 0) {
            comboTipo.getItems().stream().filter(t -> t.getId() == a.getIdTipoAula()).findFirst().ifPresent(comboTipo.getSelectionModel()::select);
        }

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Edificio:"), 0, 0); grid.add(txtEdif, 1, 0);
        grid.add(new Label("Piso:"), 0, 1); grid.add(txtPiso, 1, 1);
        grid.add(new Label("Número:"), 0, 2); grid.add(txtNum, 1, 2);
        grid.add(new Label("Capacidad:"), 0, 3); grid.add(spinCap, 1, 3);
        grid.add(new Label("Tipo Aula:"), 0, 4); grid.add(comboTipo, 1, 4);
        grid.add(new Label("Estado:"), 0, 5); grid.add(comboEst, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !txtEdif.getText().isEmpty() && comboTipo.getValue() != null) {
                a.setEdificio(txtEdif.getText().trim().toUpperCase());
                a.setPiso(txtPiso.getText().trim().toUpperCase());
                a.setNumero(txtNum.getText().trim().toUpperCase());
                a.setCapacidad(spinCap.getValue());
                a.setEstado(comboEst.getValue());
                a.setIdTipoAula(comboTipo.getValue().getId());
                aulaDAO.guardar(a);
                return true;
            }
            return false;
        });

        dialog.showAndWait().ifPresent(res -> {
            if(res) {
                cargarAulas(); // Recarga la tabla con los datos frescos y ordenados
                cargarDatosTabla(); // Si actualizaste un aula, también debe refrescarse la tabla de horarios
            }
        });
    }

    @FXML public void abrirModalNuevoTipo() { abrirEditorTipo(new app.model.entity.TipoAula()); }
    private void abrirEditorTipo(app.model.entity.TipoAula t) {
        Dialog<Boolean> dialog = new Dialog<>(); dialog.setTitle(t.getId() == 0 ? "Nuevo Tipo" : "Editar Tipo");
        TextField txtNombre = new TextField(t.getNombre());
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.add(new Label("Nombre:"), 0, 0); grid.add(txtNombre, 1, 0);
        dialog.getDialogPane().setContent(grid); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !txtNombre.getText().isEmpty()) { t.setNombre(txtNombre.getText()); tipoAulaDAO.guardar(t); return true; } return false;
        });
        dialog.showAndWait().ifPresent(res -> { if(res) cargarTipos(); });
    }

    // ==========================================
    // ELIMINACIONES (CON AVISOS CASCADA)
    // ==========================================
    private void ejecutarEliminacion(String tabla, String registro, Runnable eliminacionLogic) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "ADVERTENCIA️\nEstá a punto de eliminar '" + registro + "' de " + tabla + ".\nEsto puede borrar en cascada otros registros relacionados. ¿Seguro?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(r -> { if (r == ButtonType.YES) { eliminacionLogic.run(); cargarDatosTabla(); }});
    }

    @FXML public void eliminarMateria() {
        app.model.entity.MateriaFila s = tablaMaterias.getSelectionModel().getSelectedItem();
        if (s != null) ejecutarEliminacion("Materias", s.getNombre(), () -> { materiaDAO.eliminar(s.getId()); cargarMaterias(); cargarDatosParalelos(); });
    }
    @FXML public void eliminarDocente() {
        app.model.entity.Docente s = tablaDocentes.getSelectionModel().getSelectedItem();
        if (s != null) ejecutarEliminacion("Docentes", s.getNombre(), () -> { docenteDAO.eliminar(s.getId()); cargarDocentes(); cargarDatosParalelos(); });
    }
    @FXML public void eliminarAula() {
        app.model.entity.AulaFila s = tablaAulas.getSelectionModel().getSelectedItem();
        if (s != null) ejecutarEliminacion("Aulas", s.getNumero(), () -> { aulaDAO.eliminar(s.getId()); cargarAulas(); });
    }
    @FXML public void eliminarTipoAula() {
        app.model.entity.TipoAula s = tablaTiposAulas.getSelectionModel().getSelectedItem();
        if (s != null) ejecutarEliminacion("Tipos de Aulas", s.getNombre(), () -> { tipoAulaDAO.eliminar(s.getId()); cargarTipos(); cargarAulas(); cargarMaterias(); });
    }

    // ==========================================
    // MÓDULO IMPORTAR EXCEL
    // ==========================================
    private void logExcel(String mensaje) {
        javafx.application.Platform.runLater(() -> txtConsolaExcel.appendText(mensaje + "\n"));
    }

    @FXML
    public void descargarEjemploExcel() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Guardar Excel de Ejemplo");
        fileChooser.setInitialFileName("Ejemplo_FIQA.xlsx");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        java.io.File dest = fileChooser.showSaveDialog(mainTabPane.getScene().getWindow());
        if (dest != null) {
            try (java.io.InputStream in = getClass().getResourceAsStream("/ejemploExcel/ejemplo.xlsx")) {
                if (in == null) {
                    new Alert(Alert.AlertType.ERROR, "No se encontró 'ejemplo.xlsx' en la carpeta resources del proyecto.").show();
                    return;
                }
                java.nio.file.Files.copy(in, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                new Alert(Alert.AlertType.INFORMATION, "Excel de ejemplo guardado con éxito.").show();
            } catch (java.io.IOException e) {
                new Alert(Alert.AlertType.ERROR, "Error al guardar: " + e.getMessage()).show();
            }
        }
    }

    @FXML
    public void prepararExcelCrudo() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Seleccionar Excel Crudo");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        java.io.File inputFile = fileChooser.showOpenDialog(mainTabPane.getScene().getWindow());

        if (inputFile != null) {
            txtConsolaExcel.clear();
            progressExcel.setVisible(true);

            javafx.concurrent.Task<org.apache.poi.ss.usermodel.Workbook> tarea = new javafx.concurrent.Task<>() {
                @Override
                protected org.apache.poi.ss.usermodel.Workbook call() throws Exception {
                    return excelPrepararService.preparaExcelMemoria(inputFile, mensaje -> logExcel(mensaje));
                }
            };

            tarea.setOnSucceeded(e -> {
                progressExcel.setVisible(false);
                org.apache.poi.ss.usermodel.Workbook wbProcesado = tarea.getValue();

                Alert preg = new Alert(Alert.AlertType.CONFIRMATION,
                        "El archivo se procesó correctamente y se detectaron choques.\n¿Desea exportar el archivo con las nuevas hojas de análisis de choques?",
                        ButtonType.YES, ButtonType.NO);
                preg.setHeaderText("Análisis Completado");

                preg.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        javafx.stage.FileChooser saveChooser = new javafx.stage.FileChooser();
                        saveChooser.setTitle("Guardar Excel Procesado");
                        saveChooser.setInitialFileName(inputFile.getName().replace(".xlsx", "_procesado.xlsx"));
                        saveChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
                        java.io.File outFile = saveChooser.showSaveDialog(mainTabPane.getScene().getWindow());

                        if (outFile != null) {
                            try (java.io.FileOutputStream out = new java.io.FileOutputStream(outFile)) {
                                wbProcesado.write(out);
                                logExcel(">> Archivo guardado en: " + outFile.getAbsolutePath());
                                new Alert(Alert.AlertType.INFORMATION, "Archivo exportado con éxito.").show();
                            } catch (Exception ex) {
                                logExcel(">> Error al guardar: " + ex.getMessage());
                            }
                        }
                    }
                });
            });

            tarea.setOnFailed(e -> {
                progressExcel.setVisible(false);
                logExcel(">> ERROR FATAL: " + tarea.getException().getMessage());
            });

            new Thread(tarea).start();
        }
    }

    // importacion de datos desde un excel
    @FXML
    public void extraerEInyectarDatos() {
        // 1. ALERTA DE ADVERTENCIA CRÍTICA
        Alert advertencia = new Alert(Alert.AlertType.WARNING,
                "ATENCIÓN: Esta acción borrará todos los horarios, paralelos y docentes actuales de la base de datos " +
                        "para reemplazarlos por los datos del archivo Excel.\n\n¿Desea hacer un respaldo de su base de datos actual antes de continuar?",
                ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        advertencia.setTitle("Precaución: Reescritura de Base de Datos");

        Optional<ButtonType> res = advertencia.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.CANCEL) return;

        // 2. BACKUP (Si el usuario acepta)
        if (res.isPresent() && res.get() == ButtonType.YES) {
            javafx.stage.FileChooser saveChooser = new javafx.stage.FileChooser();
            saveChooser.setTitle("Guardar Respaldo de Base de Datos");
            saveChooser.setInitialFileName("Respaldo_FIQA.db");
            saveChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Database Files", "*.db"));
            java.io.File backupFile = saveChooser.showSaveDialog(mainTabPane.getScene().getWindow());

            if (backupFile != null) {
                try {
                    // Copiar el archivo de la DB activa (Asumiendo que se llama 'proyeccion_facultad.db')
                    java.io.File dbActual = new java.io.File("proyeccion_facultad.db");
                    java.nio.file.Files.copy(dbActual.toPath(), backupFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    logExcel(">> Respaldo guardado exitosamente en: " + backupFile.getAbsolutePath());
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Error al crear respaldo: " + e.getMessage()).show();
                    return; // Abortamos si el backup falla por seguridad
                }
            } else {
                return; // Abortar si cancela el guardado
            }
        }

        // 3. SELECCIONAR EXCEL PROCESADO E INYECTAR
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Seleccione el Excel Procesado (*.xlsx)");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.csv"));
        java.io.File inputFile = fileChooser.showOpenDialog(mainTabPane.getScene().getWindow());

        if (inputFile != null) {
            progressExcel.setVisible(true);
            txtConsolaExcel.clear();

            javafx.concurrent.Task<Void> tarea = new javafx.concurrent.Task<>() {
                @Override
                protected Void call() throws Exception {
                    extractorService.extraerEInyectar(inputFile, msg -> logExcel(msg));
                    return null;
                }
            };

            tarea.setOnSucceeded(e -> {
                progressExcel.setVisible(false);
                new Alert(Alert.AlertType.INFORMATION, "Base de datos actualizada correctamente. \nVaya a las pestañas para ver los nuevos datos.").show();
                // Opcional: Recargar tablas
                cargarMaterias(); cargarDocentes(); cargarDatosParalelos(); cargarDatosTabla();
            });

            tarea.setOnFailed(e -> {
                progressExcel.setVisible(false);
                logExcel(">> ERROR FATAL EN EXTRACCIÓN: " + tarea.getException().getMessage());
                tarea.getException().printStackTrace();
            });

            new Thread(tarea).start();
        }
    }

    // ==========================================
    // MÓDULO VISOR DE AULAS
    // ==========================================
    private void configurarVisorAulas() {
        // Llenar filtros
        comboFiltroEdificio.setItems(javafx.collections.FXCollections.observableArrayList("Todos", "E17", "E18", "E19", "E22"));
        comboFiltroEdificio.getSelectionModel().select("E17"); // Por defecto

        comboOrdenOcupacion.setItems(javafx.collections.FXCollections.observableArrayList(
                "Ordenar por Piso (Por Defecto)",
                "Más Ocupadas (Total de la Semana)",
                "Más Ocupadas (Lunes)",
                "Más Ocupadas (Martes)",
                "Más Ocupadas (Miércoles)",
                "Más Ocupadas (Jueves)",
                "Más Ocupadas (Viernes)"
        ));
        comboOrdenOcupacion.getSelectionModel().selectFirst();

        // Configurar columnas normales
        colVisAula.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("aulaDesc"));
        colVisLun.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("lunesText"));
        colVisMar.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("martesText"));
        colVisMie.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("miercolesText"));
        colVisJue.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("juevesText"));
        colVisVie.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("viernesText"));

        // NUEVO: PINTAR LA CELDA DE AULA SEGÚN SI ES COMÚN O NO
        colVisAula.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-background-color: #ecf0f1;");
                } else {
                    setText(item);
                    app.model.entity.AulaOcupacion aula = getTableView().getItems().get(getIndex());

                    // Si el tipo de aula NO contiene la palabra "COMUN" (ej. laboratorios, talleres)
                    if (aula.getTipoAula() != null && !aula.getTipoAula().toUpperCase().contains("COMUN")) {
                        setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-background-color: rgba(250,239,202,0.87); -fx-border-color: #fae6c2; -fx-border-width: 0 3px 0 0;"); // Sombreado amarillo con borde
                    } else {
                        setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-background-color: #ecf0f1;"); // Normal (Gris claro)
                    }
                }
            }
        });

        tablaVisorAulas.setFixedCellSize(javafx.scene.layout.Region.USE_COMPUTED_SIZE);

        // Configurar Multilínea (Text Wrapping) para los horarios
        configurarColumnaMultilinea(colVisLun);
        configurarColumnaMultilinea(colVisMar);
        configurarColumnaMultilinea(colVisMie);
        configurarColumnaMultilinea(colVisJue);
        configurarColumnaMultilinea(colVisVie);

        // Listeners para actualizar instantáneamente al cambiar un combo
        comboFiltroEdificio.setOnAction(e -> aplicarFiltrosYOrdenVisor());
        comboOrdenOcupacion.setOnAction(e -> aplicarFiltrosYOrdenVisor());
        chkSoloOcupadas.setOnAction(e -> aplicarFiltrosYOrdenVisor());

        cargarDatosVisorAulas();
    }

    private void configurarColumnaMultilinea(TableColumn<app.model.entity.AulaOcupacion, String> col) {
        col.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            private final javafx.scene.text.Text text = new javafx.scene.text.Text();
            private final javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(text);

            {
                text.setStyle("-fx-font-size: 11px;");
                // El ancho del texto se ajusta a la columna menos un pequeño margen
                text.wrappingWidthProperty().bind(col.widthProperty().subtract(15));
                // Le damos padding al VBox para que los horarios no se peguen a los bordes
                vbox.setPadding(new javafx.geometry.Insets(8, 5, 8, 5));
                vbox.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.trim().isEmpty()) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(vbox); // Mostramos el contenedor en lugar del texto puro
                }
            }
        });
    }

    @FXML
    public void cargarDatosVisorAulas() {
        masterOcupacion.setAll(horarioDAO.listarOcupacionAulas());
        aplicarFiltrosYOrdenVisor();
    }

    private void aplicarFiltrosYOrdenVisor() {
        String edif = comboFiltroEdificio.getValue();
        String orden = comboOrdenOcupacion.getValue();
        if (edif == null || orden == null) return;

        boolean soloOcupadas = chkSoloOcupadas.isSelected();

        // 1. Filtrar por Edificio Y por Tipo de Aula
        List<app.model.entity.AulaOcupacion> filtradas = masterOcupacion.stream()
                .filter(a -> edif.equals("Todos") || a.getEdificio().equalsIgnoreCase(edif))
                .filter(a -> !soloOcupadas || a.getHorasTotal() > 0)
                .collect(java.util.stream.Collectors.toList());

        // 2. Aplicar el Ordenamiento
        filtradas.sort((a, b) -> {
            if (orden.contains("Piso")) {
                int p1 = obtenerValorPiso(a.getPiso());
                int p2 = obtenerValorPiso(b.getPiso());
                if (p1 == p2) return a.getNumero().compareTo(b.getNumero());
                return Integer.compare(p1, p2);
            } else if (orden.contains("Total")) {
                return Integer.compare(b.getHorasTotal(), a.getHorasTotal()); // Descendente
            } else if (orden.contains("Lunes")) { return Integer.compare(b.getHorasLunes(), a.getHorasLunes());
            } else if (orden.contains("Martes")) { return Integer.compare(b.getHorasMartes(), a.getHorasMartes());
            } else if (orden.contains("Miércoles")) { return Integer.compare(b.getHorasMiercoles(), a.getHorasMiercoles());
            } else if (orden.contains("Jueves")) { return Integer.compare(b.getHorasJueves(), a.getHorasJueves());
            } else if (orden.contains("Viernes")) { return Integer.compare(b.getHorasViernes(), a.getHorasViernes());
            }
            return 0;
        });

        tablaVisorAulas.setItems(javafx.collections.FXCollections.observableArrayList(filtradas));
    }

    // Traduce los pisos a números para ordenarlos lógicamente (PB < M < P1 < P2)
    private int obtenerValorPiso(String piso) {
        if (piso == null) return 99;
        String p = piso.toUpperCase().trim();
        if (p.equals("PB")) return 0;
        if (p.equals("M")) return 1;
        if (p.startsWith("P")) {
            try { return Integer.parseInt(p.substring(1)) + 1; } catch (Exception e) {}
        }
        return 99;
    }

    @FXML
    public void exportarExcelVisor() {
        if (tablaVisorAulas.getItems().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No hay datos visibles en la tabla para exportar.").show();
            return;
        }

        // 1. Mensaje de confirmación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Se exportará un Excel estructurado con los registros actualmente visibles y los filtros aplicados en la tabla.\n¿Desea continuar?",
                ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText("Exportar Reporte de Aulas");

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                // 2. Ventana de Explorador de Archivos nativa
                javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                fileChooser.setTitle("Guardar Reporte de Aulas");
                fileChooser.setInitialFileName("Ocupacion_Aulas_Filtrado.xlsx");
                fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

                File file = fileChooser.showSaveDialog(mainTabPane.getScene().getWindow());

                if (file != null) {
                    try {
                        // 3. Ejecutar servicio (pasando los ítems actualmente visibles)
                        new app.model.service.ExcelExportarAulasService().exportarVisor(tablaVisorAulas.getItems(), file);

                        new Alert(Alert.AlertType.INFORMATION, "¡Excel exportado exitosamente en:\n" + file.getAbsolutePath()).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        new Alert(Alert.AlertType.ERROR, "Error al crear el archivo Excel: " + e.getMessage()).show();
                    }
                }
            }
        });
    }

    @FXML
    public void volverInicio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
            Parent root = loader.load();
            mainTabPane.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}