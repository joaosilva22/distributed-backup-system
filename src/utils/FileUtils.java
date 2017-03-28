package utils;

import files.FileMetadata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.NoSuchFileException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class FileUtils {
    private FileUtils() {}
    
    private static byte[] getBitString(String filepath) throws IOException {
        Path path = Paths.get(filepath);
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

        StringBuilder builder = new StringBuilder();
        builder.append(path.getFileName().toString());
        builder.append(attr.lastModifiedTime());
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    private static byte[] sha256(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input);
    }

    private static String bytesToAsciiString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte current : bytes) {
            String formatted = String.format("%02X", current);
            builder.append(formatted);
        }
        return builder.toString();
    }

    public static String getFileId(String filepath) {
        String output = "";
        try {
            byte[] input = FileUtils.getBitString(filepath);
            byte[] hash = FileUtils.sha256(input);
            output = FileUtils.bytesToAsciiString(hash);
        } catch (IOException e) {
            IOUtils.log("FileUtils error: " + e.toString());
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            IOUtils.log("FileUtils error: " + e.toString());
            e.printStackTrace();
        }
        return output;
    }

    // TODO: Neste momento o tamanho de cada chunk e sempre 64000,
    //       independentemente do numero de bytes que estao no chunk.
    public static ArrayList<byte[]> getFileChunks(String filepath, int size) {
        ArrayList<byte[]> chunks = new ArrayList<>();
        int numRead = 0, read = 0;
        try {
            InputStream stream = new FileInputStream(filepath);
            byte[] temp = new byte[size];
            while ((read = stream.read(temp)) != -1) {
                byte[] chunk = Arrays.copyOfRange(temp, 0, read);
                chunks.add(chunk);
                numRead += read;
            }
            if (numRead % size == 0) {
                byte[] chunk = new byte[0];
                chunks.add(chunk);
            }
        } catch (FileNotFoundException e) {
            IOUtils.log("FileUtils error: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            IOUtils.log("FileUtils error: " + e.toString());
            e.printStackTrace();
        }
        return chunks;
    }

    public static void createFile(String filepath, byte[] data) throws IOException, FileNotFoundException {
        File file = new File(filepath);
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(file, false);
        if (data != null) {
            out.write(data);
        }
        out.close();
    }

    public static byte[] readFile(String filepath) throws IOException {
        return Files.readAllBytes(Paths.get(filepath));
    }

    public static void writeToFile(String filepath, byte[] data) throws IOException {
        FileOutputStream out = new FileOutputStream(filepath, true);
        out.write(data);
        out.close();
    }

    public static void deleteFile(String filepath) throws NoSuchFileException, DirectoryNotEmptyException, IOException {
        Files.delete(Paths.get(filepath));
    }

    public static void setFileMetadata(String filepath, FileMetadata metadata) throws IOException {
        FileTime lastModifiedTime = metadata.getLastModifiedTime();
        Files.setLastModifiedTime(Paths.get(filepath), lastModifiedTime);
    }
}
