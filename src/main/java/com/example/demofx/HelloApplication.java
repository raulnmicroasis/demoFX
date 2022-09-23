package com.example.demofx;

import java.awt.Desktop;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;

public class HelloApplication extends Application {

    private final Desktop desktop = Desktop.getDesktop();
    private final String pathExcel = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\img\\iconoExcel.png";
    private final String pathTXT = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\img\\iconoTexto.png";
    private final String pathNoExtension = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\img\\iconoSinExtension.png";
    private final String pathBin = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\img\\bin.png";
    private final String pathLoading = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\img\\loading.gif";
    private final String pathSubido = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\img\\subido.png";
    private final String pathError = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\img\\error.png";
    private static final double WIDTH = 800;
    private static final double HEIGHT = 600;
    private static final double IMG_WIDTH = 35;
    private static final double IMG_HEIGHT = 35;
    private static final double MARGIN = 25;
    ObservableList<TableItem> items;
    private TableView<TableItem> table;
    private ProcesosLectura lectura;
    private boolean insertar;
    private boolean ambitoCorrecto;
    private Database db;
    private Workbook wb;

    /* Imagenes */
    ImageView excel = new ImageView(new Image(new FileInputStream(pathExcel)));
    ImageView txt = new ImageView(new Image(new FileInputStream(pathTXT)));
    ImageView noExtension = new ImageView(new Image(new FileInputStream(pathNoExtension)));
    ImageView bin = new ImageView(new Image(new FileInputStream(pathBin)));

    public HelloApplication() throws FileNotFoundException {
    }


    @Override
    public void start(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(new Group(), WIDTH, HEIGHT);
        Group root = (Group) scene.getRoot();
        items = FXCollections.observableArrayList();
        lectura = new ProcesosLectura();
        /* Instanciar conexión con base de datos */
        db = new Database();
        db.connection = db.conexionDB();

        /* Detalle imagenes */
        excel.setFitWidth(IMG_WIDTH);
        excel.setFitHeight(IMG_HEIGHT);
        txt.setFitWidth(IMG_WIDTH);
        txt.setFitHeight(IMG_HEIGHT);
        noExtension.setFitWidth(IMG_WIDTH);
        noExtension.setFitHeight(IMG_HEIGHT);
        bin.setFitHeight(IMG_HEIGHT);
        bin.setFitWidth(IMG_WIDTH);

        /* Barra de herramientas */
        ToolBar toolBar = new ToolBar();
        Button bArchivo = new Button("Archivo");
        Button bAmbito = new Button("Seleccionar área de trabajo");
        toolBar.getItems().add(bArchivo);
        toolBar.getItems().add(bAmbito);
        toolBar.getItems().add(new Separator());
        toolBar.setPrefWidth(WIDTH);

        /* Button Events */
        bArchivo.setOnAction(actionEvent -> {
            /* File chooser */
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Escoja un archivo para importar.");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
            List<File> files = fileChooser.showOpenMultipleDialog(stage);
            resetTableView(files);
        });

        bAmbito.setOnAction(actionEvent -> {
            db.idGrupo = 81L;
            db.idEmpresa = 107L;
            db.idLocal = 116L;
            ambitoCorrecto = db.consultaAmbito("81", "107", "116");
        });

        /* Table View con ficheros */
        table = new TableView<>();

        TableColumn<TableItem, ImageView> column1 = new TableColumn<>("EXTENSION");
        column1.setPrefWidth(IMG_WIDTH + 5);
        column1.setCellValueFactory(new PropertyValueFactory<>("imagen"));
        TableColumn<TableItem, ImageView> column2 = new TableColumn<>("FILE NAME");
        column2.setPrefWidth(250);
        column2.setCellValueFactory(new PropertyValueFactory<>("path"));
        TableColumn<TableItem, ImageView> column3 = new TableColumn<>("");
        column3.setPrefWidth(60);
        column3.setCellValueFactory(new PropertyValueFactory<>("button"));
        TableColumn<TableItem, ImageView> column4 = new TableColumn<>("");
        column4.setPrefWidth(IMG_WIDTH + 5);
        column4.setCellValueFactory(new PropertyValueFactory<>("estado"));

        table.getColumns().add(column1);
        table.getColumns().add(column2);
        table.getColumns().add(column3);
        table.getColumns().add(column4);
        table.setPrefWidth(WIDTH - MARGIN * 2);
        table.setLayoutX(MARGIN);
        table.setLayoutY(MARGIN * 2);

        /* Botón comenzar traspaso */
        Button bStart = new Button("INICIAR TRASPASO");
        bStart.setLayoutX(WIDTH / 2 - 100);
        bStart.setLayoutY(HEIGHT - 140);
        bStart.setPrefWidth(200);
        bStart.setPrefHeight(100);

        bStart.setOnAction(actionEvent -> {
            /* Procesos previos a la lectura de ficheros */

            /* Ordenar por prioridad */
            ordenarFicheros();
            String nombreArchivo;

            /* Recorrer array de ficheros para leer uno por uno */
            for (TableItem file : items) {
                try {

                    /* Instanciar objeto de lectura sobre Excel */
                    try {
                        wb = new XSSFWorkbook(file.getFichero());
                    } catch (IOException | InvalidFormatException e) {
                        db.cerrarConexionDb();
                        break;
                    }

                    /* Comprobar qué método de lectura/inserción debe seguir */
                    nombreArchivo = file.getPath().toLowerCase();
                    if (nombreArchivo.contains("clientes")) {
                        file.setEstado(new ImageView(new Image(new FileInputStream(pathLoading), IMG_WIDTH, IMG_HEIGHT, true, true)));
                        insertar = lectura.lecturaCliente(wb, db);
                    }
                    else if (nombreArchivo.contains("prove")) {
                        file.setEstado(new ImageView(new Image(new FileInputStream(pathLoading), IMG_WIDTH, IMG_HEIGHT, true, true)));
                        insertar = lectura.lecturaProv(wb, db);
                    }
                    else if (nombreArchivo.contains("prevcobr")){
                        file.setEstado(new ImageView(new Image(new FileInputStream(pathLoading), IMG_WIDTH, IMG_HEIGHT, true, true)));
                        insertar = lectura.lecturaRecibos(wb, db);
                    }
                    else if (nombreArchivo.contains("linfactu")){
                        file.setEstado(new ImageView(new Image(new FileInputStream(pathLoading), IMG_WIDTH, IMG_HEIGHT, true, true)));
                        insertar = lectura.lecturaPSuministro(wb, db);
                    }
                    else if (nombreArchivo.contains("obras")){
                        file.setEstado(new ImageView(new Image(new FileInputStream(pathLoading), IMG_WIDTH, IMG_HEIGHT, true, true)));
                        insertar = lectura.lecturaFactura(wb, db);
                    }
                    else if (nombreArchivo.contains("cobic")){
                        file.setEstado(new ImageView(new Image(new FileInputStream(pathLoading), IMG_WIDTH, IMG_HEIGHT, true, true)));
                        insertar = lectura.updateBanco(wb, db);
                    }
                    else if (nombreArchivo.contains("bancos")){
                        file.setEstado(new ImageView(new Image(new FileInputStream(pathLoading), IMG_WIDTH, IMG_HEIGHT, true, true)));
                        insertar = lectura.lecturaBancos(wb, db);
                    }

                    /* Comprobar que no hubo error en la última lectura/inserción */
                    try {
                        if (!insertar) {
                            file.setEstado(new ImageView(new Image(new FileInputStream(pathError), IMG_WIDTH, IMG_HEIGHT, true, true)));
                            db.connection.rollback();
                            break;
                        } else {
                            file.setEstado(new ImageView(new Image(new FileInputStream(pathSubido), IMG_WIDTH, IMG_HEIGHT, true, true)));
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } finally {
                    table.setItems(items);
                }
            }
        });

        root.getChildren().add(toolBar);
        root.getChildren().add(table);
        root.getChildren().add(bStart);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    private void ordenarFicheros() {
        String nombreFichero;
        ObservableList<TableItem> auxiliar = FXCollections.observableArrayList();
        for (TableItem file : items) {
            nombreFichero = file.getPath().toLowerCase();
            if (nombreFichero.contains("client") || nombreFichero.contains("art") || nombreFichero.contains("prov")) {
                auxiliar.add(0, file);
            } else {
                auxiliar.add(file);
            }
        }
        for (int j = auxiliar.size() - 1; j >= 0; j--) {
            nombreFichero = auxiliar.get(j).getPath().toLowerCase();
            if (nombreFichero.contains("fact")) {
                auxiliar.add(auxiliar.get(j));
                auxiliar.remove(j);
            }
        }
        for (int j = auxiliar.size() - 1; j >= 0; j--) {
            nombreFichero = auxiliar.get(j).getPath().toLowerCase();
            if (nombreFichero.contains("apun") || nombreFichero.contains("iva") || nombreFichero.contains("prevcobr") || nombreFichero.contains("existen") || nombreFichero.contains("compra")) {
                auxiliar.add(auxiliar.get(j));
                auxiliar.remove(j);
            }
        }
        items = auxiliar;
        table.setItems(items);
    }

    public static void main(String[] args) {
        launch();
    }

    public void resetTableView(List<File> files) {

        for (File file : files) {
            try {
                String fileType = Files.probeContentType(file.toPath());
                String pathDescargado = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\img\\descargado.png";

                switch (fileType) {
                    case "text/plain" ->
                            items.add(new TableItem(new ImageView(new Image(new FileInputStream(pathTXT), IMG_WIDTH, IMG_HEIGHT, true, true)), file.getName(), new ImageView(new Image(new FileInputStream(pathDescargado), IMG_WIDTH, IMG_HEIGHT, true, true)), file));
                    case "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ->
                            items.add(new TableItem(new ImageView(new Image(new FileInputStream(pathExcel), IMG_WIDTH, IMG_HEIGHT, true, true)), file.getName(), new ImageView(new Image(new FileInputStream(pathDescargado), IMG_WIDTH, IMG_HEIGHT, true, true)), file));
                    default ->
                            items.add(new TableItem(new ImageView(new Image(new FileInputStream(pathNoExtension), IMG_WIDTH, IMG_HEIGHT, true, true)), file.getName(), new ImageView(new Image(new FileInputStream(pathDescargado), IMG_WIDTH, IMG_HEIGHT, true, true)), file));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        table.setItems(items);
    }

    public class TableItem {

        private ImageView imagen;
        private String path;
        private Button button;
        private ImageView estado;

        private File fichero;

        public TableItem(ImageView imagen, String path, ImageView estado, File fichero) throws FileNotFoundException {
            this.imagen = imagen;
            this.path = path;
            this.button = createButton();
            this.estado = estado;
            this.fichero = fichero;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public ImageView getImagen() {
            return imagen;
        }

        public void setImagen(ImageView imagen) {
            this.imagen = imagen;
        }

        public Button getButton() {
            return button;
        }

        public void setButton(Button button) {
            this.button = button;
        }

        public ImageView getEstado() {
            return estado;
        }

        public void setEstado(ImageView estado) {
            this.estado = estado;
        }

        public File getFichero() {
            return fichero;
        }

        public void setFichero(File fichero) {
            this.fichero = fichero;
        }

        public Button createButton() throws FileNotFoundException {
            Button button = new Button();
            button.setGraphic(new ImageView(new Image(new FileInputStream(pathBin), IMG_WIDTH, IMG_HEIGHT, true, true)));
            button.setPrefWidth(IMG_WIDTH);
            button.setPrefHeight(IMG_HEIGHT);
            button.setOnAction(actionEvent -> {
                /* Eliminar de la tabla */
                items.remove(this);
                table.setItems(items);
            });
            return button;
        }
    }


}