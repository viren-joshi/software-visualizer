package com.g8.properties;

public class FileProps {

    private static String filePath;

    public static String getFilePath() {
        return filePath;
    }

    public static void setFilePath(String filePath) {
        FileProps.filePath = filePath;
    }
}
