package dev.vloos.javahccs;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @FXML Button btnBrowsePath, btnBrowseReportPath, btnScan;
    @FXML TextField txtScanPath, txtReportPath;
    @FXML CheckBox chkScanSubfolders;
    @FXML ProgressBar progressBar;

    @Override
    public void start(Stage stage) throws IOException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        Scene scene = new Scene(root, 425, 190);
        stage.setResizable(false);
        stage.setTitle("Java HCCS");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void startScan() throws Exception {
        Scan.run(txtScanPath.getText(), txtReportPath.getText(), chkScanSubfolders.isSelected(), progressBar);
    }

    @FXML
    private void browseOutput() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        JFileChooser jf = new JFileChooser();
        int choice = chooser.showOpenDialog(null);
        if (choice != JFileChooser.APPROVE_OPTION) return;
        txtReportPath.setText(chooser.getSelectedFile().toString());
    }

    @FXML
    private void browsePath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        JFileChooser jf = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Jar files", "jar");
        chooser.setFileFilter(filter);
        int choice = chooser.showOpenDialog(null);
        if (choice != JFileChooser.APPROVE_OPTION) return;
        txtScanPath.setText(chooser.getSelectedFile().toString());
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
