// PROG2 VT2023, Inlämningsuppgift, del 2
// Grupp 031
// Hanna Arrhenius haar9434
// Erik Strandberg erst1916
// Robin Westling rowe7856
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

public class PathFinder extends Application{
    private static final String FILE_NAME = "europa.graph";
    private static final String IMAGE_URL = "file:europa.gif";
    private ListGraph<Location> listGraph = new ListGraph<>();
    private StringBuilder connectionsList = new StringBuilder();
    private String locationsListAsString = "";
    private boolean eventHandlerActivated = false;
    private boolean mapIsLoaded = false;
    private boolean changed = false;
    private Image map = new Image(IMAGE_URL);
    private ImageView imageView;
    private StackPane stackPane;
    private Pane outputArea;
    private String[] locationsArray;
    private String[] connectionsArray;
    private Location selectedLocation1;
    private Location selectedLocation2;
    private boolean continueAction = false;


    public void start(Stage stage){

        //Create StackPane to hold the image and circles
        imageView = new ImageView();
        stackPane = new StackPane();
        stackPane.getChildren().add(imageView);

        outputArea = new Pane();
        stackPane.getChildren().add(outputArea);

        //Drop down menu
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem newMapMI = new MenuItem("New Map");
        MenuItem openMI = new MenuItem("Open");
        MenuItem saveMI = new MenuItem("Save");
        MenuItem saveImageMI = new MenuItem("Save Image");
        MenuItem exitMI = new MenuItem("Exit");

        newMapMI.setOnAction(event -> {
            checkForChanges(event);
            if(continueAction){
                newMap(stage, IMAGE_URL);
                changed = true;
                continueAction=false;
            }
        });

        openMI.setOnAction(event -> {
            checkForChanges(event);
            if(continueAction){
                open(stage);
                continueAction=false;
            }
        });

        saveMI.setOnAction(new SaveHandler());
        saveImageMI.setOnAction(new SaveImageHandler(stackPane));

        exitMI.setOnAction(event -> {
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
        fileMenu.getItems().addAll(newMapMI, openMI, saveMI, saveImageMI, exitMI);
        menuBar.getMenus().add(fileMenu);

        //Buttons
        Button findPathButton = new Button("Find Path");
        Button showConnectionButton = new Button("Show Connection");
        Button newPlaceButton = new Button("New Place");
        Button newConnectionButton = new Button("New Connection");
        Button changeConnectionButton = new Button("Change Connection");

        findPathButton.setOnAction(event -> findPath());
        showConnectionButton.setOnAction(event -> showConnection());
        newConnectionButton.setOnAction(event -> connect());
        changeConnectionButton.setOnAction(event -> changeConnection());

        VBox vbox = new VBox();
        HBox buttons = new HBox();

        ///New Place
        newPlaceButton.setOnAction(event -> {
            if(mapIsLoaded){
                eventHandlerActivated = true;
                stackPane.setCursor(Cursor.CROSSHAIR);
                newPlaceButton.setDisable(true);
            }
        });

        stage.setOnCloseRequest(event -> {
            if(changed){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText("Unsaved changes, continue anyway?");
                Optional<ButtonType> result = alert.showAndWait();
                if(result.isPresent() && result.get() == ButtonType.CANCEL){
                    event.consume();
                }
            }

        });

        stackPane.setOnMouseClicked(event -> {
            newPlace(event, newPlaceButton);
        });

        buttons.getChildren().addAll(findPathButton, showConnectionButton, newPlaceButton, newConnectionButton, changeConnectionButton);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(5));
        buttons.setSpacing(10);

        menuBar.setId("menu");
        fileMenu.setId("menuFile");
        newMapMI.setId("menuNewMap");
        openMI.setId("menuOpenFile");
        saveMI.setId("menuSaveFile");
        saveImageMI.setId("menuSaveImage");
        exitMI.setId("menuExit");

        findPathButton.setId("btnFindPath");
        showConnectionButton.setId("btnShowConnection");
        newPlaceButton.setId("btnNewPlace");
        changeConnectionButton.setId("btnChangeConnection");
        newConnectionButton.setId("btnNewConnection");
        outputArea.setId("outputArea");

        vbox.getChildren().addAll(menuBar, buttons, stackPane);

        Scene scene = new Scene(new Group(vbox), Color.WHITE);
        stage.setScene(scene);
        stage.setTitle("PathFinder");
        stage.show();
    }
    public static void main(String[] args) {
        Application.launch(args);
    }

    private void newPlace(MouseEvent mouseEvent, Button button){
        if (eventHandlerActivated) {
            changed = true;
            stackPane.setCursor(Cursor.DEFAULT);
            button.setDisable(false);

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New Place");
            dialog.setHeaderText("Enter the name of the new place:");
            dialog.setContentText("Name:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                changed = true;
                Location location = new Location(name, mouseEvent.getX(), mouseEvent.getY());
                location.setOnMouseClicked(new ClickerHandler());
                outputArea.getChildren().add(location);
                listGraph.add(location);
                location.toFront();
            });
            eventHandlerActivated = false;
        }
    }
    private void open(Stage stage) {
        try {
            FileReader fileReader = new FileReader(FILE_NAME);
            BufferedReader lineReader = new BufferedReader(fileReader);
            String imageUrl = lineReader.readLine();
            newMap(stage, imageUrl);
            locationsListAsString = lineReader.readLine();

            connectionsList = new StringBuilder();
            String line;
            while ((line = lineReader.readLine()) != null) {
                connectionsList.append(line).append(";");
            }

            lineReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        locationsArray = locationsListAsString.split(";");

        for (int i = 0; i < locationsArray.length - 2; i += 3) {
            double x1 = Double.parseDouble(locationsArray[i + 1]);
            double y1 = Double.parseDouble(locationsArray[i + 2]);
            Location newLocation = new Location(locationsArray[i], x1, y1);
            newLocation.setOnMouseClicked(new ClickerHandler());
            newLocation.setFill(Color.BLUE);
            listGraph.add(newLocation);
            outputArea.getChildren().add(newLocation);
            newLocation.toFront();
        }

        connectionsArray = connectionsList.toString().split(";");

        if (connectionsArray.length != 0) {

            for (int i = 0; i < connectionsArray.length - 3; i += 4) {
                Location destination1 = null;
                Location destination2 = null;

                for (Location city : listGraph.getNodes()) {
                    if (city.getName().equals(connectionsArray[i])) {
                        destination1 = city;
                    }
                    if (city.getName().equals(connectionsArray[i + 1])) {
                        destination2 = city;
                    }
                }

                if (destination1 != null && destination2 != null) {
                    if(listGraph.getEdgeBetween(destination1, destination2) == null){
                        int weight = Integer.parseInt(connectionsArray[i + 3]);
                        listGraph.connect(destination1, destination2, connectionsArray[i + 2], weight);

                        Line drawLine = new Line(destination1.getCenterX(), destination1.getCenterY(), destination2.getCenterX(), destination2.getCenterY());
                        outputArea.getChildren().add(drawLine);
                        drawLine.setDisable(true);
                    }
                }
            }
        }
    }
    private void newMap(Stage stage, String imageUrl){
        selectedLocation1 = null;
        selectedLocation2 = null;
        map = new Image(imageUrl);
        imageView = new ImageView(map);

        imageView.setImage(map);
        imageView.setFitHeight(map.getHeight());
        imageView.setFitWidth(map.getWidth());
        imageView.setPreserveRatio(true);

        stackPane.setMinWidth(map.getWidth());
        stackPane.setMinHeight(map.getHeight());

        outputArea.getChildren().clear(); // Clear any existing circles
        outputArea.getChildren().add(imageView);

        // Set the size of the StackPane and all its children to the size of the image
        stackPane.setPrefSize(map.getWidth(), map.getHeight());
        stackPane.setMaxSize(map.getWidth(), map.getHeight());
        stackPane.setMinSize(map.getWidth(), map.getHeight());

        stage.sizeToScene();
        mapIsLoaded = true;
        ArrayList<Location> locationArrayList = new ArrayList<>(listGraph.getNodes());
        for (Location location : locationArrayList){
            listGraph.remove(location);
        }
    }
    private void connect(){
        if (selectedLocation1 == null || selectedLocation2 == null){
            showAlert("Two places must be selected!");
            return;
        }
        if (listGraph.getEdgeBetween(selectedLocation1, selectedLocation2) != null){
            showAlert("A connection already exists between the selected places!");
            return;
        }
        //Connect
        ConnectionAlert connectionAlert = new ConnectionAlert();
        connectionAlert.showAndWait().ifPresent(buttonType -> {
            if(buttonType == ButtonType.OK){
                listGraph.connect(selectedLocation1, selectedLocation2, connectionAlert.getName(), connectionAlert.getTime());
                Line drawLine = new Line(selectedLocation1.getCenterX(), selectedLocation1.getCenterY(), selectedLocation2.getCenterX(), selectedLocation2.getCenterY());
                outputArea.getChildren().add(drawLine);
                drawLine.setDisable(true);
                changed = true;
            }
        });
    }
    private void changeConnection(){
        if (selectedLocation1 == null || selectedLocation2 == null){
            showAlert("Two places must be selected!");
            return;
        }
        if(listGraph.getEdgeBetween(selectedLocation1, selectedLocation2) == null){
            showAlert("No connection found!");
            return;
        }
        ConnectionAlert connectionAlert = new ConnectionAlert(listGraph.getEdgeBetween(selectedLocation1, selectedLocation2), false);
        connectionAlert.showAndWait().ifPresent(buttonType -> {
            if(buttonType == ButtonType.OK) listGraph.setConnectionWeight(selectedLocation1, selectedLocation2, connectionAlert.getTime());
            changed = true;
        });
    }
    private void showConnection(){
        if (selectedLocation1 == null || selectedLocation2 == null){
            showAlert("Two places must be selected!");
            return;
        }
        if(listGraph.getEdgeBetween(selectedLocation1, selectedLocation2) == null){
            showAlert("No connection found!");
            return;
        }
        ConnectionAlert connectionAlert = new ConnectionAlert(listGraph.getEdgeBetween(selectedLocation1, selectedLocation2), true);
        connectionAlert.showAndWait();
    }
    private void findPath(){
        if(listGraph.getPath(selectedLocation1, selectedLocation1) == null){
            showAlert("No path found!");
            return;
        }
        StringBuilder stringBuilder;
        stringBuilder = new StringBuilder();
        int count = 0;
        for(Edge<Location> e : listGraph.getPath(selectedLocation1, selectedLocation2)){
            stringBuilder.append(e.toString()).append("\n");
            count += e.getWeight();
        }
        stringBuilder.append("Total " + count);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText("The path from " + selectedLocation1 + " to " + selectedLocation2 + ":");
        alert.setContentText(stringBuilder.toString());
        alert.showAndWait();
    }
    private void showAlert(String message) {
        Alert msgBox = new Alert(Alert.AlertType.ERROR);
        msgBox.setTitle("Error!");
        msgBox.setHeaderText(null);
        msgBox.setContentText(message);
        msgBox.showAndWait();
    }
    private void checkForChanges(Event event){
        if(changed){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Unsaved changes, continue anyway?");
            Optional<ButtonType> result = alert.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.CANCEL){
                event.consume();
            }else{
                continueAction = true;
            }
        }else{continueAction = true;}
    }
    static class ConnectionAlert extends Alert{
        private final TextField nameField = new TextField();
        private final TextField timeField = new TextField();
        private GridPane gridPane;
        ConnectionAlert(){
            super(AlertType.CONFIRMATION);
            setUpGrid();
            getDialogPane().setContent(gridPane);
        }
        ConnectionAlert(Edge<Location> edge, boolean displayOnly){
            super(AlertType.CONFIRMATION);
            setUpGrid();
            nameField.setEditable(false);
            nameField.setText(edge.getName());
            if(displayOnly){
                timeField.setEditable(false);
                timeField.setText(String.valueOf(edge.getWeight()));
            }else {
                timeField.setEditable(true);
            }
            getDialogPane().setContent(gridPane);
        }
        private void setUpGrid(){
            gridPane = new GridPane();
            gridPane.setAlignment(Pos.CENTER);
            gridPane.setPadding(new Insets(10));
            gridPane.setHgap(5);
            gridPane.setVgap(10);
            gridPane.addRow(0, new Label("Name:"), nameField);
            gridPane.addRow(1, new Label("Time:"), timeField);
        }
        public String getName(){
            return nameField.getText();
        }
        public int getTime(){
            return Integer.parseInt(timeField.getText());
        }
    }
    class SaveHandler implements EventHandler<ActionEvent> {
        private String citiesString = "";
        private String edgesString = "";
        @Override
        public void handle(ActionEvent actionEvent) {
            try {
                citiesString = "";
                edgesString = "";
                FileWriter writer = new FileWriter("europa.graph");
                PrintWriter printWriter = new PrintWriter(writer);
                boolean first = false;
                for (Location location : listGraph.getNodes()) {
                    if (!first) {
                        citiesString += location.saveInformation();
                        first = true;
                    } else {
                        citiesString += ";" + location.saveInformation();
                    }
                    for (var v : listGraph.getEdgesFrom(location)) {
                        edgesString += location.getName() + ";" + v.getDestination().getName() + ";" + v.getName() + ";" + v.getWeight() + "\n";
                    }
                }
                printWriter.println("file:europa.gif");
                printWriter.println(citiesString);
                printWriter.println(edgesString);
                writer.close();
                printWriter.close();
            } catch (IOException ignored) {
            }
        }
    }
    class ClickerHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            // Skapar en temporär cirkel vilket blir cirklen man precis tryckt på.
            Location location = (Location) event.getSource();

            // Kollar om cirkelmarked1 är tom och att c (cirkeln man tryckt) på inte är lika med circlemarked2.
            // Tilldelar sedan c till cirkelmarked1.
            if (selectedLocation1 == null && !location.equals(selectedLocation2)) {
                selectedLocation1 = location;
                selectedLocation1.setFill(Color.RED); // RÖD


            }
            // Kollar om cirkelmarked2 är tom och att c (cirkeln man tryckt) på inte är lika med circlemarked1.
            // Tilldelar sedan c till cirkelmarked2.
            else if (selectedLocation2 == null && !location.equals(selectedLocation1)) {
                selectedLocation2 = location;
                selectedLocation2.setFill(Color.RED); // RÖD
            }
            // Kollar om c (cirkeln man tryckt) är lika med circleMarked1 och inte lika med circleMarked2 och
            // isåfall omarkerar circleMarked1 genom att göra den blå och sätta den till null.
            else if (location.equals(selectedLocation1) && !location.equals(selectedLocation2)) {
                location.setFill(Color.BLUE); // BLÅ
                selectedLocation1 = null;
            }
            // Kollar om c (cirkeln man tryckt) är lika med circleMarked2 och inte lika med circleMarked1 och
            // isåfall omarkerar circleMarked2 genom att göra den blå och sätta den till null.
            else if (location.equals(selectedLocation2) && !location.equals(selectedLocation1)) {
                location.setFill(Color.BLUE); // BLÅ
                selectedLocation2 = null;
            }
        }
    }
    static class Location extends Circle {
        private String name;

        Location(String name, double centerX, double centerY) {
            super(centerX, centerY, 10);
            this.name = name;
            this.setFill(Color.BLUE);
            this.setId(name);
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String toString(){
            return name;
        }
        public String saveInformation(){
            return name + ";" + getCenterX() + ";" + getCenterY();
        }
    }
    static class SaveImageHandler implements EventHandler<ActionEvent> {
        private final StackPane stackPane;

        SaveImageHandler(StackPane stackPane) {
            this.stackPane = stackPane;
        }

        public void handle(ActionEvent event) {
            try {
                WritableImage image = stackPane.snapshot(new SnapshotParameters(), null);
                File file = new File("capture.png");
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bufferedImage, "png", file);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("IO-fel: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
