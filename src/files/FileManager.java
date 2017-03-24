package files;

import main.DistributedBackupService;
import utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class FileManager {
    private HashMap<String, FileData> files;
    
    public FileManager() {
        new File(FileManagerConstants.PATH).mkdir();
        files = new HashMap<>();
    }

    public FileData getFile(String fileId) {
        return files.get(fileId);
    }

    public void registerChunk(int serverId, String fileId, int chunkNo, int replicationDeg) {
        // TODO: Quando um peer inicia o protocolo de putchunk, com
        //       um ficheiro que ja deu backup (identificado pelo fileId),
        //       atualizar os metadados desse ficheiro? Ou entao dar um erro
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
        if (fileData == null) {
            return null;
        }
        ChunkData chunk = file.getChunk(chunkNo);
        if (chunk == null) {
            return null;
        }

        String path = FileManagerConstants.PATH + getChunkFileName(fileId, chunkNo);
        try {
            byte[] data = FileUtils.readFile(path);
        } catch (IOException e) {
            IOUtils.err("FileManager err: " + e.toString);
            e.printStackTrace();
            return null;
        }
        return data;
    }

    private String getChunkFileName(String fileId, int chunkNo) {
        return fileId + "_" + chunkNo;
    }
}
