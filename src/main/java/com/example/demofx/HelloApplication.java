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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class HelloApplication extends Application {

    private final Desktop desktop = Desktop.getDesktop();
    private final String pathExcel = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\iconoExcel2.png";
    private final String pathTXT = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\iconoTexto.png";
    private final String pathNoExtension = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\iconoSinExtension.png";
    private final String pathBin = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\bin.png";
    private final String pathLoading = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\loading.gif";
    private static final double WIDTH = 800;
    private static final double HEIGHT = 600;
    private static final double IMG_WIDTH = 35;
    private static final double IMG_HEIGHT = 35;
    private static final double MARGIN = 25;
    ObservableList<TableItem> items;
    private TableView<TableItem> table;

    /* Imagenes */
    ImageView excel = new ImageView(new Image(new FileInputStream(pathExcel)));
    ImageView txt = new ImageView(new Image(new FileInputStream(pathTXT)));
    ImageView noExtension = new ImageView(new Image(new FileInputStream(pathNoExtension)));
    ImageView bin = new ImageView(new Image(new FileInputStream(pathBin)));
    ImageView gif = new ImageView(new Image(new FileInputStream(pathLoading)));

    public HelloApplication() throws FileNotFoundException {
    }


    @Override
    public void start(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(new Group(), WIDTH, HEIGHT);
        Group root = (Group) scene.getRoot();
        items = FXCollections.observableArrayList();

        /* Detalle imagenes */
        excel.setFitWidth(IMG_WIDTH);
        excel.setFitHeight(IMG_HEIGHT);
        txt.setFitWidth(IMG_WIDTH);
        txt.setFitHeight(IMG_HEIGHT);
        noExtension.setFitWidth(IMG_WIDTH);
        noExtension.setFitHeight(IMG_HEIGHT);
        bin.setFitHeight(IMG_HEIGHT);
        bin.setFitWidth(IMG_WIDTH);
        gif.setFitHeight(IMG_HEIGHT);
        gif.setFitWidth(IMG_WIDTH);

        /* Barra de herramientas */
        ToolBar toolBar = new ToolBar();
        Button bArchivo = new Button("Archivo");
        toolBar.getItems().add(bArchivo);
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

        /* Table View con ficheros */
        table = new TableView<>();

        TableColumn<TableItem, ImageView> column1 = new TableColumn<>("EXTENSION");
        column1.setPrefWidth(IMG_WIDTH+5);
        column1.setCellValueFactory(new PropertyValueFactory<>("imagen"));
        TableColumn<TableItem, ImageView> column2 = new TableColumn<>("FILE NAME");
        column2.setPrefWidth(250);
        column2.setCellValueFactory(new PropertyValueFactory<>("path"));
        TableColumn<TableItem, ImageView> column3 = new TableColumn<>("");
        column3.setPrefWidth(60);
        column3.setCellValueFactory(new PropertyValueFactory<>("button"));
        TableColumn<TableItem, ImageView> column4 = new TableColumn<>("");
        column4.setPrefWidth(IMG_WIDTH+5);
        column4.setCellValueFactory(new PropertyValueFactory<>("gif"));

        table.getColumns().add(column1);
        table.getColumns().add(column2);
        table.getColumns().add(column3);
        table.getColumns().add(column4);
        table.setPrefWidth(WIDTH-MARGIN*2);
        table.setLayoutX(MARGIN);
        table.setLayoutY(MARGIN*2);

        /* Botón comenzar traspaso */
        Button bStart = new Button("INICIAR TRASPASO");
        bStart.setLayoutX(WIDTH/2-100);
        bStart.setLayoutY(HEIGHT-140);
        bStart.setPrefWidth(200);
        bStart.setPrefHeight(100);

        bStart.setOnAction(actionEvent -> {
            /* Procesos previos a la lectura de ficheros */
            /* Ordenar por prioridad */
            ordenarFicheros();
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

                switch (fileType) {
                    case "text/plain" -> items.add(new TableItem(new ImageView(new Image(new FileInputStream(pathTXT), IMG_WIDTH, IMG_HEIGHT, true, true)), file.getName(), new ImageView(new Image(new FileInputStream(pathLoading), IMG_WIDTH, IMG_HEIGHT, true, true))));
                    case "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ->
                            items.add(new TableItem(new ImageView(new Image(new FileInputStream(pathExcel), IMG_WIDTH, IMG_HEIGHT, true, true)), file.getName(), new ImageView(new Image(new FileInputStream(pathLoading), IMG_WIDTH, IMG_HEIGHT, true, true))));
                    default -> items.add(new TableItem(new ImageView(new Image(new FileInputStream(pathNoExtension), IMG_WIDTH, IMG_HEIGHT, true, true)), file.getName(), new ImageView(new Image(new FileInputStream(pathLoading), IMG_WIDTH, IMG_HEIGHT, true, true))));
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
        private ImageView gif;

        public TableItem(ImageView imagen) {
            this.imagen = imagen;
        }

        public TableItem(ImageView img, String path) {
            this.imagen = img;
            this.path = path;
        }

        public TableItem(ImageView imagen, String path, ImageView gif) throws FileNotFoundException {
            this.imagen = imagen;
            this.path = path;
            this.button = createButton();
            this.gif = gif;
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

        public ImageView getGif() {
            return gif;
        }

        public void setGif(ImageView gif) {
            this.gif = gif;
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