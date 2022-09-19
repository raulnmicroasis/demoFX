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

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;

public class HelloApplication extends Application {

    private final Desktop desktop = Desktop.getDesktop();
    private ArrayList<File> ficheros;
    private static final double WIDTH = 800;
    private static final double HEIGHT = 600;

    ObservableList<TableItem> items;

    private TableView<TableItem> table;

    public static class TableItem extends File{

        public Image imagen;

        public TableItem(String pathname) {
            super(pathname);
        }

        public TableItem(String parent, String child) {
            super(parent, child);
        }

        public TableItem(File parent, String child) {
            super(parent, child);
        }

        public TableItem(URI uri) {
            super(uri);
        }

        public Image getImagen() {

            try {
                String fileType = Files.probeContentType(this.toPath());
                switch (fileType) {
                    case "text/plain" -> {
                        return new Image("demofx/iconoTexto.jpg");
                    }
                    case "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> {
                        return new Image("demofx/iconoExcel.png");
                    }
                    default -> {
                        return new Image("demofx/iconoSinExtension.png");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void setImagen(Image imagen) {
            this.imagen = imagen;
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(new Group(), WIDTH, HEIGHT);
        Group root = (Group) scene.getRoot();
        ficheros = new ArrayList<>();
        items = FXCollections.observableArrayList();

        /* Barra de herramientas */
        ToolBar toolBar = new ToolBar();
        Button bArchivo = new Button("Archivo");
        toolBar.getItems().add(bArchivo);
        toolBar.getItems().add(new Separator());

        /* Button Events */
        bArchivo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                /* File chooser */
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Escoja un archivo para importar.");
                File file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    ficheros.add(file);
                    resetTableView();
                }
            }
        });

        /* Table View con ficheros */
        table = new TableView<>();

        TableColumn<TableItem, Image> column1 = new TableColumn<>("FILES");
        column1.setPrefWidth(WIDTH);
        column1.setCellValueFactory(new PropertyValueFactory<>("imagen"));

        table.getColumns().add(column1);

        /* Scroll Bar */
        ScrollBar sb = new ScrollBar();
        sb.setOrientation(Orientation.VERTICAL);

        /* Imagen */
        ObservableList<Node> seq = root.getChildren();
        Image imagen = new Image("com/example/demofx/iconoTexto.jpg");
        addImageToObservableList(seq, 160, 20, 420, 120, imagen,
                createImageCursor(imagen.getUrl(), 16, 16));

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

    public void resetTableView() {
        items.add(new TableItem(ficheros.get(ficheros.size()-1).getPath()));
        table.setItems(items);
    }

    private static void addImageToObservableList(ObservableList<Node> seq,
                                                 int x, int y,
                                                 int w, int h,
                                                 Image image,
                                                 Cursor cursor) {
        ImageView imageView = new ImageView();
        imageView.setX(x);
        imageView.setY(y);
        imageView.setFitWidth(w);
        imageView.setFitHeight(h);
        imageView.setPreserveRatio(true);
        imageView.setImage(image);
        imageView.setCursor(cursor);
        seq.add(imageView);
    }

    private static Cursor createImageCursor(final String url,
                                            final float hotspotX,
                                            final float hotspotY) {

        final Image cursorImage = new Image(url, 32, 32, false, true, true);
        return new ImageCursor(cursorImage, hotspotX, hotspotY);
    }


}