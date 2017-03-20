package service;

import java.io.File;

public class FileManager {
    private Hashmap<String, FileData> files;
    
    public FileManager() {
        new File(FileManagerConstants.PATH).mkdir();
    }
}
