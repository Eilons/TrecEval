package com.Technion.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.List;

public class Utils {

    public static void ifFalseCrash(boolean val, String message) {
        if (! val) {
            System.out.println(message);
            System.exit(-1);
        }
    }

    public static String[] splitBySpace(String row) {
        return  row.split("\\s+");
    }

    public static void walk(File candidate, List<File> allFiles ) {

        if (candidate.isFile()) {
            if (FilenameUtils.getExtension(candidate.getName()).equals("res")) {
                allFiles.add(candidate);
            }
        }

        File[] list = candidate.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if (f.isDirectory()) {
                walk(f,allFiles);
            } else {
                allFiles.add(f);
            }

        }
    }

    public static void writeString (String content, String outPath, boolean append) {
        try{
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter
                    (new FileOutputStream(outPath,append),"UTF-8"));
            wr.write(content);
            wr.newLine();
            wr.flush();
            wr.close();
        } catch (Exception ex) {
            System.out.println("Error while trying to write the evaluation file" + ex.toString());
        }
    }

    public static List<String> readLines (File file) {
        List<String> fileLines = null;
        try {
            fileLines = FileUtils.readLines(file,"UTF-8");
        } catch (IOException e) {
            System.out.println("Error reading file: " + file.getAbsolutePath());
            e.printStackTrace();
            System.exit(1);
        }
        return fileLines;
    }
}
