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
import java.nio.file.NoSuchFileException;
import java.nio.file.DirectoryNotEmptyException;

public class FileManager implements Serializable {
    private HashMap<String, FileData> files;

    // TODO: Isto devia ser uma constante
    private int storageSpace = 64000;
    private int usedSpace = 0;
    
    public FileManager() {
        new File(FileManagerConstants.PATH).mkdir();
        files = new HashMap<>();
    }

    public void init() {
        for (String fileId : files.keySet()) {
            if (files.get(fileId).getMetadata() == null) {
                for (int chunkNo : files.get(fileId).getChunks().keySet()) {
                    String filename = getChunkFileName(fileId, chunkNo);
                    File f = new File(FileManagerConstants.PATH + filename);
                    if (!f.exists()) {
                        usedSpace -= files.get(fileId).getChunks().get(chunkNo).getSize();
                        files.get(fileId).getChunks().remove(chunkNo);
                        IOUtils.warn("FileManager warning: Lost chunk data <" + fileId + ", " + chunkNo + ">");
                    }
                }
            }
        }
    }

    public FileData getFile(String fileId) {
        return files.get(fileId);
    }

    public void registerFile(String filepath, String fileId) {
        FileData file = getFile(fileId);
        if (file == null) {
            try {
                files.put(fileId, new FileData(new FileMetadata(filepath, fileId)));
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
            String filepath = FileManagerConstants.PATH + getChunkFileName(fileId, chunkNo);
            FileUtils.createFile(filepath, data);
            file.newChunk(chunkNo, serverId, replicationDeg);
            if (data != null) {
                file.getChunk(chunkNo).setSize(data.length);
                usedSpace += data.length;
            }
        }
    }

    public void recoverChunk(String fileId, int chunkNo, byte[] data) {
        FileData file = getFile(fileId);
        if (file != null) {
            ChunkData chunk = file.getChunk(chunkNo);
            if (chunk == null) {
                chunk = new ChunkData();
                file.getChunks().put(chunkNo, chunk);
            }
            chunk.setData(data);
        }
    }

    public void incrementReplicationDeg(int serverId, String fileId, int chunkNo) {
        FileData file = getFile(fileId);
        if (file == null) {
            return;
        }
        ChunkData chunk = file.getChunk(chunkNo);
        if (chunk != null) {
            chunk.incrementReplicationDegree(serverId);
        }
    }

    public void decreaseReplicationDegree(int serverId, String fileId, int chunkNo) {
        FileData file = getFile(fileId);
        if (file == null) {
            return;
        }
        ChunkData chunk = file.getChunk(chunkNo);
        if (chunk != null) {
            chunk.decreaseReplicationDegree(serverId);
        }
    }

    public int getChunkReplicationDegree(String fileId, int chunkNo) {
        ChunkData chunk = getChunk(fileId, chunkNo);
        if (chunk != null) {
            return chunk.getReplicationDegree();
        }
        return -1;
    }

    public int getChunkDesiredReplicationDegree(String fileId, int chunkNo) {
        ChunkData chunk = getChunk(fileId, chunkNo);
        if (chunk != null) {
            return chunk.getDesiredReplicationDegree();
        }
        return -1;
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
            IOUtils.err("FileManager error: " + e.toString());
            e.printStackTrace();
            return null;
        }
        return data;
    }

    private String getChunkFileName(String fileId, int chunkNo) {
        return fileId + "_" + chunkNo;
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

    public FileMetadata getFileMetadata(String filepath) {
        for (FileData data : files.values()) {
            if (data.getMetadata().getFilepath().equals(filepath)) {
                return data.getMetadata();
            }
        }
        return null;
    }

    public void deleteChunkFile(String fileId, int chunkNo) {
        String filepath = FileManagerConstants.PATH + getChunkFileName(fileId, chunkNo);
        try {
            FileUtils.deleteFile(filepath);
        } catch (NoSuchFileException e) {
            IOUtils.err("FileManager error: " + e.toString());
            e.printStackTrace();
        } catch (DirectoryNotEmptyException e) {
            IOUtils.err("FileManager error: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            IOUtils.err("FileManager error: " + e.toString());
            e.printStackTrace();
        }
    }

    public void deleteFile(String fileId) {
        files.remove(fileId);
    }

    public void addUsedSpace(int diff) {
        usedSpace += diff;
    }

    public ChunkData getChunk(String fileId, int chunkNo) {
        FileData file = files.get(fileId);
        if (file != null) {
            return file.getChunks().get(chunkNo);
        }
        return null;
    }
}
