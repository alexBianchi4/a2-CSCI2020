/**
 * CSCI2020 Assignment 2: File server
 * By: Alex Bianchi & Ahamd Mujeeb
 */

package sample;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;

public class FileClient extends Application {
    private Stage stage;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private InputStream fileReader;
    public File localDir;
    public File[] localFiles;

    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;
        stage.setTitle("assignment 2");
        initialize();
        chooseDir();
    }

    /**
     * Creates the socket that connects to the server
     * Creates the filereader
     * @throws Exception
     */
    public void initialize() throws Exception{
        clientSocket = new Socket("localHost",8080);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(),true);
        fileReader = clientSocket.getInputStream();
    }

    /**
     * lets the user choose their shared directory from within the program
     */
    public void chooseDir(){
        // path label set to null when no directory is selected
        Text path = new Text("null");
        path.setFill(Color.rgb(72,61,139));
        path.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        // create a DirectoryChooser to pick the directory with the data in it
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("src"));

        // DirectoryChooser opens on button press
        Button pickDir = new Button("Select Local Directory");
        pickDir.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");
        pickDir.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                localDir = directoryChooser.showDialog(stage);
                path.setText(localDir.getAbsolutePath());
            }
        });

        // create a button that moves on to the next stage of the program
        Button submit = new Button("Submit");
        submit.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");
        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                //button does not work unless a directory has been chosen
                if(localDir != null){
                    localFiles = localDir.listFiles();
                    makeClient();
                }
            }
        });

        // clears the path chosen by the user
        Button clear = new Button("Clear");
        clear.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");
        clear.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                path.setText("null");
                localDir = null;
            }
        });

        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.getChildren().addAll(submit,clear);

        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.getChildren().addAll(pickDir,path);

        VBox vbox2 = new VBox();
        vbox2.setStyle("-fx-border-color: black");
        vbox2.setStyle("-fx-background-color: Gainsboro");
        vbox2.setSpacing(10);
        vbox2.getChildren().addAll(vbox,hbox);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vbox2);
        vbox2.setAlignment(Pos.CENTER);
        vbox.setAlignment(Pos.CENTER);
        hbox.setAlignment(Pos.BOTTOM_CENTER);

        stage.setScene(new Scene(borderPane,600,500));
        stage.show();
    }

    /**
     * creates the client UI
     * The client handler waits for a button to be pressed
     */
    private void makeClient(){
        // the layout for the scene is a vbox
        VBox SceneLayout = new VBox();

        // declare the upload and download button and put them in a hbox
        Button upload = new Button("Upload");
        upload.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");
        Button download = new Button("Download");
        download.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");
        Button dir = new Button("Directory");
        dir.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");
        HBox buttonContainer = new HBox();
        buttonContainer.getChildren().addAll(upload,download,dir);
        buttonContainer.setSpacing(5);

        // create the ListViews and put them in a hbox so it can be resized dynamically
        ListView localList = new ListView();
        // loads this list with the files on the system
        for(File f: localFiles){
            localList.getItems().add(f.getName());
        }
        // this list has it's contents loaded when the user presses dir or uploads a file
        ListView serverList = new ListView();
        HBox listContainer = new HBox();
        listContainer.getChildren().addAll(localList,serverList);

        // add everything to the vbox
        SceneLayout.getChildren().addAll(buttonContainer,listContainer);
        localList.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
        serverList.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);

        // make properties grow with the vbox
        VBox.setVgrow(listContainer, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(localList,javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(serverList,javafx.scene.layout.Priority.ALWAYS);

        // event handler for the upload button
        upload.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                // gets the index of the selected object in the list when the upload button is pressed
                ObservableList selectedIndices = localList.getSelectionModel().getSelectedIndices();
                for(Object o : selectedIndices){
                    try {
                        // get the path of the object (file) that was selected in the list
                        File input = new File(localDir+"\\"+localFiles[(int)o].getName());
                        String fileName = localFiles[(int)o].getName();

                        // send the client handler an upload message
                        out.println("UPLOAD");
                        // send the client handler the name of the file being uploaded
                        out.println(fileName);
                        FileInputStream fileReader = new FileInputStream(input.getAbsolutePath());
                        byte[] bytes = new byte[20000];
                        // convert the file into an array of bytes
                        fileReader.read(bytes, 0, bytes.length);
                        // put the bytes into a stream format and send to server
                        OutputStream os = clientSocket.getOutputStream();
                        os.write(bytes, 0, bytes.length);

                        // update the listView for the server
                        serverList.getItems().clear();
                        out.println("DIR");
                        try{
                            while(true){
                                String name = in.readLine();
                                if(name.equals("quit")){
                                    break;
                                }else{
                                    serverList.getItems().add(name);
                                }
                            }
                        }catch(IOException e){}
                    }
                    catch(FileNotFoundException e) {
                        System.err.println("File not found. Please scan in new file.");
                    }
                    catch (IOException e){}

                }
            }
        });

        // event handler for the download button
        download.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                // gets the index of the selected object in the list when the upload button is pressed
                ObservableList selectedIndices = serverList.getSelectionModel().getSelectedIndices();
                for(Object o : selectedIndices){
                    try {
                        String fileName = serverList.getItems().get((int)o).toString();
                        // create the path that the file will be copied to
                        File output = new File(localDir+"\\"+fileName);

                        // send the client handler the download command
                        out.println("DOWNLOAD");
                        // send the name of the file being downloaded
                        out.println(fileName);
                        // convert the file into an array of bytes
                        byte[] bytes = new byte[20000];
                        FileOutputStream fileOut = new FileOutputStream(output.getAbsolutePath());
                        fileReader.read(bytes,0,bytes.length);
                        fileOut.write(bytes,0,bytes.length);

                        // update the listView
                        localFiles = localDir.listFiles();
                        localList.getItems().clear();
                        for(File f: localFiles){
                            localList.getItems().add(f.getName());
                        }
                    }
                    catch(FileNotFoundException e) {
                        System.err.println("File not found. Please scan in new file.");
                    }
                    catch(IOException e){}
                }
            }
        });
        dir.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                out.println("DIR");
                serverList.getItems().clear();
                try{
                    while(true){
                        String name = in.readLine();
                        if(name.equals("quit")){
                            break;
                        }else{
                            serverList.getItems().add(name);
                        }
                    }
                }catch(IOException e){}
            }
        });

        // display the scene
        stage.setScene(new Scene(SceneLayout,600,600));
        stage.show();
    }

    public static void main(String[] args) throws FileNotFoundException {
        launch(args);
    }
}
