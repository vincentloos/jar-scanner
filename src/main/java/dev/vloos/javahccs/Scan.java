package dev.vloos.javahccs;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import org.apache.commons.io.FileUtils;
import jd.core.Decompiler;
import jd.core.DecompilerException;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class Scan {

    public static void run(String inputPath, String outputPath, boolean scanSubfolders, ProgressBar progressBar) throws Exception {
        File inputFile = new File(inputPath);
        if (inputFile.exists() && inputFile.isDirectory()) {
            if (scanSubfolders) {
                List<File> files = (List<File>) FileUtils.listFiles(inputFile, new String[]{"jar"}, true);
                scan(files, progressBar, outputPath);
            } else {
                File[] files = inputFile.listFiles((d, name) -> name.endsWith(".jar"));
                scan(Arrays.asList(files), progressBar, outputPath);
                return;
            }
        } else if (inputFile.exists() && inputFile.isFile()) {
            scan(Arrays.asList(inputFile), progressBar, outputPath);
            return;
        } else {
            JOptionPane.showMessageDialog(null, "Could not find input file or folder '" + inputPath + "'", "Java HCCS", JOptionPane.PLAIN_MESSAGE);
            return;
        }
    }

    private static void scan(List<File> files, ProgressBar progressBar, String outputPath) throws Exception {
        if (files.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Directory does not contain jar files", "Java HCCS", JOptionPane.PLAIN_MESSAGE);
            return;
        } else if (!(new File(outputPath).exists()) || !(new File(outputPath).isDirectory()) ){
            JOptionPane.showMessageDialog(null, "Invalid output directory!", "Java HCCS", JOptionPane.PLAIN_MESSAGE);
            return;
        }

        new Thread() {
            public void run() {

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
                        String[] triggers = {"password", "pass", "pswd", "pswrd", "credentials", "cred", "creds", "login"};
                        for (int j = 0; j < lines.length; j++) {
                            if (Arrays.stream(triggers).parallel().anyMatch(lines[j].toLowerCase()::contains)) {
                                try(FileWriter fw = new FileWriter(outputPath + "\\Java-HCCS Report.txt", true);
                                    BufferedWriter bw = new BufferedWriter(fw);
                                    PrintWriter out = new PrintWriter(bw))
                                {
                                    out.println("Possible hit on '" + files.get(i).getPath() + "'," +
                                            "\nin class '" + name + "':" +
                                            "\n" + lines[j].replace("  ", "") + "\n");
                                } catch (IOException e) {
                                    //exception handling left as an exercise for the reader
                                }
                            }
                        }
                    }
                }
                progressBar.setProgress(1.0);
                try {
                    Desktop.getDesktop().open(new File(outputPath + "\\Java-HCCS Report.txt"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
