package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {
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
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return output;
    }

    // FIXME: Neste momento o tamanho de cada chunk e sempre 64000,
    // independentemente do numero de bytes que estao no chunk.
    public static ArrayList<byte[]> getFileChunks(String filepath, int size) {
        ArrayList<byte[]> chunks = new ArrayList<>();
        try {
            InputStream stream = new FileInputStream(filepath);
            byte[] chunk = new byte[size];
            chunks.add(chunk);
            while (stream.read(chunk) != -1) {
                chunk = new byte[size];
                chunks.add(chunk);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chunks;
    }

    // TODO: Apagar isto (eventualmente)
    public static void main(String[] args) {
        try {
            byte[] input = FileUtils.getBitString("/home/joaosilva/todo.org");
            byte[] hash = FileUtils.sha256(input);
            String output = FileUtils.bytesToAsciiString(hash);
            System.out.println(output.length() + " : " + output);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(FileUtils.getFileId("/home/joaosilva/todo.org"));

        ArrayList<byte[]> chunks = FileUtils.getFileChunks("/home/joaosilva/todo.org", 64000);
        for (int i = 0; i < chunks.size(); i++) {
            System.out.println("chunks[" + i + "].length=" + chunks.get(i).length);
        }

        for (byte[] chunk : chunks) {
            for (byte c : chunk) {
                System.out.printf("%c", c);
            }
            System.out.printf("<EOC>\n");
        }
    }
}
