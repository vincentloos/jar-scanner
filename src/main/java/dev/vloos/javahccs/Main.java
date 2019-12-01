package dev.vloos.javahccs;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

    public Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }

    @FXML Button btnBrowseScanPath, btnBrowseScanDirectoryPath, btnBrowseReportPath, btnScan;
    @FXML TextField txtScanPath, txtReportPath, txtTriggers;
    @FXML CheckBox chkScanSubfolders;
    @FXML ProgressBar progressBar;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        Scene scene = new Scene(root, 590, 230);
        stage.setResizable(false);
        stage.getIcons().add(new Image("/icon.png"));
        stage.setTitle("Java HCCS");
        stage.setScene(scene);
        stage.show();
        mainStage = stage;
    }

    @FXML
    private void startScan() {
        try {
            Scan.run(txtScanPath.getText(), txtReportPath.getText(), chkScanSubfolders.isSelected(), progressBar, txtTriggers.getText().split(","));
        } catch (Exception e) {}
    }

    @FXML
    private void browseOutput() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select output directory");
        txtReportPath.setText(chooser.showDialog(mainStage).getAbsolutePath());
    }

    @FXML
    private void browsePath() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select file to scan");
        chooser.getExtensionFilters().addAll(new ExtensionFilter("JAR Files", "*.jar"));
        txtScanPath.setText(chooser.showOpenDialog(mainStage).getAbsolutePath());
        updateScanPath();
    }

    @FXML
    private void browseDirectoryPath() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select directory to scan");
        txtScanPath.setText(chooser.showDialog(mainStage).getAbsolutePath());
        updateScanPath();
    }

    @FXML
    private void updateScanPath() {
        File file = new File(txtScanPath.getText());
        if (file.exists() && file.isDirectory()) {
            chkScanSubfolders.setDisable(false);
        } else {
            chkScanSubfolders.setSelected(false);
            chkScanSubfolders.setDisable(true);
        }

    }

}
