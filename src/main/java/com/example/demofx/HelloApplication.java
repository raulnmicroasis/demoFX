package com.example.demofx;

import java.awt.Desktop;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
    private static final double WIDTH = 800;
    private static final double HEIGHT = 600;

    private static final double IMG_WIDTH = 100;
    private static final double IMG_HEIGHT = 100;

    ObservableList<TableItem> items;

    private TableView<TableItem> table;

    /* Imagenes */
    TableItem excel = new TableItem(new ImageView(new Image(new FileInputStream("C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\iconoExcel.png"))));
    TableItem txt = new TableItem(new ImageView(new Image(new FileInputStream("C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\iconoTexto.jpg"))));
    TableItem noExtension = new TableItem(new ImageView(new Image(new FileInputStream("C:\\Users\\HP\\IdeaProjects\\demoFX\\src\\main\\java\\com\\example\\demofx\\iconoSinExtension.png"))));

    public HelloApplication() throws FileNotFoundException {
    }


    @Override
    public void start(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(new Group(), WIDTH, HEIGHT);
        Group root = (Group) scene.getRoot();
        items = FXCollections.observableArrayList();

        /* Detalle imagenes */
        excel.getImagen().setFitWidth(IMG_WIDTH);
        excel.getImagen().setFitHeight(IMG_HEIGHT);
        txt.getImagen().setFitWidth(IMG_WIDTH);
        txt.getImagen().setFitHeight(IMG_HEIGHT);
        noExtension.getImagen().setFitWidth(IMG_WIDTH);
        noExtension.getImagen().setFitHeight(IMG_HEIGHT);

        /* Barra de herramientas */
        ToolBar toolBar = new ToolBar();
        Button bArchivo = new Button("Archivo");
        toolBar.getItems().add(bArchivo);
        toolBar.getItems().add(new Separator());

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

        TableColumn<TableItem, ImageView> column1 = new TableColumn<>("FILES");
        column1.setPrefWidth(WIDTH);
        column1.setCellValueFactory(new PropertyValueFactory<>("imagen"));

        table.getColumns().add(column1);

        /* Scroll Bar */
        ScrollBar sb = new ScrollBar();
        sb.setOrientation(Orientation.VERTICAL);

        VBox vbox = new VBox(toolBar, table);
        root.getChildren().add(sb);
        root.getChildren().add(vbox);
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
                case "text/plain" -> items.add(txt);
                case "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ->
                        items.add(excel);
                default -> items.add(noExtension);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        table.setItems(items);
    }

    public static class TableItem{

        private ImageView imagen;

        public TableItem(ImageView img) {
            this.imagen = img;
        }

        public ImageView getImagen() {
            return imagen;
        }

        public void setImagen(ImageView imagen) {
            this.imagen = imagen;
        }
    }


}