package com.example.demofx;

import java.awt.Desktop;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

public class HelloApplication extends Application {

    private final Desktop desktop = Desktop.getDesktop();

    private final String pathExcel = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\iconoExcel2.png";

    private final String pathTXT = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\iconoTexto.png";

    private final String pathNoExtension = "C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\iconoSinExtension.png";

    private static final double WIDTH = 800;
    private static final double HEIGHT = 600;

    private static final double IMG_WIDTH = 100;
    private static final double IMG_HEIGHT = 100;

    private static final double MARGIN = 25;

    ObservableList<TableItem> items;

    private TableView<TableItem> table;

    /* Imagenes */
    ImageView excel = new ImageView(new Image(new FileInputStream(pathExcel)));
    ImageView txt = new ImageView(new Image(new FileInputStream(pathTXT)));
    ImageView noExtension = new ImageView(new Image(new FileInputStream(pathNoExtension)));

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
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                resetTableView(file);
            }
        });

        /* Table View con ficheros */
        table = new TableView<>();

        TableColumn<TableItem, ImageView> column1 = new TableColumn<>("FILE EXTENSION");
        column1.setPrefWidth(IMG_WIDTH);
        column1.setCellValueFactory(new PropertyValueFactory<>("imagen"));
        TableColumn<TableItem, ImageView> column2 = new TableColumn<>("FILE NAME");
        column2.setPrefWidth(250);
        column2.setCellValueFactory(new PropertyValueFactory<>("path"));

        table.getColumns().add(column1);
        table.getColumns().add(column2);
        table.setPrefWidth(WIDTH-MARGIN*2);
        table.setLayoutX(MARGIN);
        table.setLayoutY(MARGIN*2);

        /* Scroll Bar */
        ScrollBar sb = new ScrollBar();
        sb.setOrientation(Orientation.VERTICAL);

        root.getChildren().add(toolBar);
        root.getChildren().add(table);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public void resetTableView(File file) {

        try {
            String fileType = Files.probeContentType(file.toPath());

            switch (fileType) {
                case "text/plain" -> items.add(new TableItem(txt, file.getName()));
                case "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ->
                        items.add(new TableItem(excel, file.getName()));
                default -> items.add(new TableItem(noExtension, file.getName()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        table.setItems(items);
    }

    public static class TableItem{

        private ImageView imagen;

        private String path;

        public TableItem(ImageView imagen) {
            this.imagen = imagen;
        }

        public TableItem(ImageView img, String path) {
            this.imagen = img;
            this.path = path;
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
    }


}