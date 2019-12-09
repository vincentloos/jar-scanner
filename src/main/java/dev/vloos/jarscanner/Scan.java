package dev.vloos.jarscanner;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import org.apache.commons.io.FileUtils;
import jd.core.Decompiler;

import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class Scan {

    public static void run(String inputPath, String outputPath, boolean scanSubfolders, ProgressBar progressBar, String[] triggers) throws Exception {
        File inputFile = new File(inputPath);
        if (inputFile.exists() && inputFile.isDirectory()) {
            if (scanSubfolders) {
                List<File> files = (List<File>) FileUtils.listFiles(inputFile, new String[]{"jar"}, true);
                scan(files, progressBar, outputPath, triggers);
            } else {
                File[] files = inputFile.listFiles((d, name) -> name.endsWith(".jar"));
                scan(Arrays.asList(files), progressBar, outputPath, triggers);
                return;
            }
        } else if (inputFile.exists() && inputFile.isFile()) {
            scan(Arrays.asList(inputFile), progressBar, outputPath, triggers);
            return;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("JAR Scanner");
            alert.setHeaderText("Invalid value");
            alert.setContentText("Could not find input file or folder");
            alert.showAndWait();
            return;
        }
    }

    private static void scan(List<File> files, ProgressBar progressBar, String outputPath, String[] triggers) {
        if (files.equals(null) || files.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("JAR Scanner");
            alert.setHeaderText("Invalid value");
            alert.setContentText("Directory does not contain jar files");
            alert.showAndWait();
            return;
        } else if (!(new File(outputPath).exists()) || !(new File(outputPath).isDirectory()) ){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("JAR Scanner");
            alert.setHeaderText("Invalid value");
            alert.setContentText("Output directory does not exist");
            alert.showAndWait();
            return;
        }

        new Thread() {
            public void run() {
                String reportId = UUID.randomUUID().toString().substring(0, 4);
                String reportName = outputPath + "\\JAR-Scanner-Report-" + reportId + ".txt";
                try {
                    new PrintWriter(reportName, "UTF-8").close();
                } catch (FileNotFoundException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                for (
                        int i = 0; i < files.size(); i++) {
                    final double step = i * 1.0;
                    Platform.runLater(() -> progressBar.setProgress(step / files.size()));

                    JarFile jarFile = null;
                    try {
                        jarFile = new JarFile(files.get(i));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Enumeration allEntries = jarFile.entries();

                    while (allEntries.hasMoreElements()) {
                        JarEntry entry = (JarEntry) allEntries.nextElement();
                        String name = entry.getName();
                        if (!name.endsWith(".class")) continue;

                        String decompiled = "";
                        try {
                            decompiled = new Decompiler().decompileClass(files.get(i).getPath(), name);
                        } catch (Exception e) {
                            continue;
                        }
                        String[] lines = decompiled.split("\r?\n|\r");
                        for (int j = 0; j < lines.length; j++) {
                            if (Arrays.stream(triggers).parallel().anyMatch(lines[j].toLowerCase()::contains)) {
                                try(FileWriter fw = new FileWriter(reportName, true);
                                    BufferedWriter bw = new BufferedWriter(fw);
                                    PrintWriter out = new PrintWriter(bw))
                                {
                                    out.println("Possible hit on '" + files.get(i).getPath() + "'," +
                                            "\nin class '" + name + "':" +
                                            "\n" + lines[j].replace("  ", "") + "\n");
                                } catch (IOException e) {}
                            }
                        }
                    }
                }
                progressBar.setProgress(1.0);
                try {
                    Desktop.getDesktop().open(new File(reportName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("JAR Scanner");
        alert.setHeaderText("Scan started");
        alert.setContentText("You just started a new scan. This may take a while depending on the size. When the scan finishes the report will automatically open.");
        alert.show();
    }

}
