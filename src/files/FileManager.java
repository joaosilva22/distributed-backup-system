package files;

import main.DistributedBackupService;
import utils.FileUtils;
import utils.IOUtils;
import utils.Tuple;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Vector;

public class FileManager implements Serializable {
    private HashMap<String, FileData> files;
    private Vector<Tuple<String, Integer>> expectedChunks;

    // TODO: Isto devia ser uma constante
    private int storageSpace = 63000;
    private int usedSpace = 0;
    
    public FileManager() {
        new File(FileManagerConstants.PATH).mkdir();
        files = new HashMap<>();
        expectedChunks = new Vector<>();
    }

    public FileData getFile(String fileId) {
        return files.get(fileId);
    }

    public void registerFile(String filepath, String fileId) {
        FileData file = getFile(fileId);
        if (file == null) {
            try {
                files.put(fileId, new FileData(new FileMetadata(filepath)));
            } catch (IOException e) {
                IOUtils.err("FileManager error: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    public void registerChunk(String fileId, int chunkNo, int replicationDeg) {
        FileData file = getFile(fileId);
        if (file == null) {
            files.put(fileId, new FileData());
            file = getFile(fileId);
        }
        ChunkData chunk = file.getChunk(chunkNo);
        if (chunk == null) {
            file.newChunk(chunkNo, replicationDeg);
        }
    }

    public void saveChunk(int serverId, String fileId, int chunkNo, int replicationDeg, byte[] data) throws IOException {        
        FileData file = getFile(fileId);
        if (file == null) {
            files.put(fileId, new FileData());
            file = getFile(fileId);
        }

        ChunkData chunk = file.getChunk(chunkNo);
        if (chunk == null) {
            // TODO: Criar um novo ficheiro para guardar o chunk
            //       (tipo fileManager.saveChunk(data) ou assim)
            //       e guardar os metadados desse chunk no hashmap
            String filepath = FileManagerConstants.PATH + getChunkFileName(fileId, chunkNo);
            FileUtils.createFile(filepath, data);
            file.newChunk(chunkNo, serverId, replicationDeg);
            if (data != null) {
                file.getChunk(chunkNo).setSize(data.length);
                usedSpace += data.length;
            }
        }
    }

    public void incrementReplicationDeg(int serverId, String fileId, int chunkNo) {
        FileData file = getFile(fileId);
        if (file == null) {
            return;
        }
        ChunkData chunk = file.getChunk(chunkNo);
        if (chunk != null) {
            chunk.incrementReplicationDeg(serverId);
        }
    }

    public byte[] retrieveChunkData(String fileId, int chunkNo) {
        FileData file = getFile(fileId);
        if (file == null) {
            return null;
        }
        ChunkData chunk = file.getChunk(chunkNo);
        if (chunk == null) {
            return null;
        }

        byte[] data;
        String path = FileManagerConstants.PATH + getChunkFileName(fileId, chunkNo);
        try {
            data = FileUtils.readFile(path);
        } catch (IOException e) {
            IOUtils.err("FileManager err: " + e.toString());
            e.printStackTrace();
            return null;
        }
        return data;
    }

    private String getChunkFileName(String fileId, int chunkNo) {
        return fileId + "_" + chunkNo;
    }

    public void expectChunk(String fileId, int chunkNo) {
        expectedChunks.add(new Tuple<>(fileId, chunkNo));
    }

    public boolean isExpectingChunk(String fileId, int chunkNo) {
        return expectedChunks.contains(new Tuple<>(fileId, chunkNo));
    }

    public int getAvailableSpace() {
        return storageSpace - usedSpace;
    }

    public void save(int serverId) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(FileManagerConstants.PATH + FileManagerConstants.METADATA_FILENAME + serverId);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(this);
        out.close();
        fileOut.close();

        IOUtils.log("Saving file metadata...");
    }
}
