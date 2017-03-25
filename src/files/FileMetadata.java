package files;

import java.io.Serializable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class FileMetadata implements Serializable {
    private String filepath;
    private long size;
    private long lastModifiedTime;
    private long lastAccessTime;
    private long creationTime;
    
    public FileMetadata(String filepath) throws IOException {
        Path path = Paths.get(filepath);
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        size = attr.size();
        lastModifiedTime = attr.lastModifiedTime().toMillis();
        lastAccessTime = attr.lastAccessTime().toMillis();
        creationTime = attr.creationTime().toMillis();
    }
}
