package dev.vloos.javahccs;

import javafx.scene.control.ProgressBar;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class Scan {

    public static void run(String inputPath, String outputPath, boolean scanSubfolders, int scanIntensity, ProgressBar progressBar) throws IOException {
        File inputFile = new File(inputPath);
        if (inputFile.exists() && inputFile.isDirectory()) {
            if (scanSubfolders) {
                List<File> files = (List<File>) FileUtils.listFiles(inputFile, new String[]{"jar"}, true);
                scan(files);
            } else {
                File[] files = inputFile.listFiles((d, name) -> name.endsWith(".jar"));
                scan(Arrays.asList(files));
                return;
            }
        } else if (inputFile.exists() && inputFile.isFile()) {
            scan(Arrays.asList(inputFile));
            return;
        } else {
            System.out.println("Could not find input file or folder '" + inputPath + "'");
            return;
        }
    }

    private static void scan(List<File> files) {
        System.out.println(files);
    }

}
