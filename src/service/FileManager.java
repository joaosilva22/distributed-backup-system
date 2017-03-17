package service;

import java.io.File;

public class FileManager {
    
    public FileManager() {
        new File(FileManagerConstants.PATH).mkdir();
    }
}
