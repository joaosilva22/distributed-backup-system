package files;

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

    public void saveChunk(String fileId, int chunkNo, int replicationDeg, byte[] data) throws IOException {
        FileData file = getFile(fileId);
        if (file == null) {
            file = files.put(fileId, new FileData());
        }

        ChunkData chunk = file.getChunk(chunkNo);
        if (chunk == null) {
            // TODO: Criar um novo ficheiro para guardar o chunk
            //       (tipo fileManager.saveChunk(data) ou assim)
            //       e guardar os metadados desse chunk no hashmap
            String filepath = FileManagerConstants.PATH + fileId + "_" + chunkNo;
            FileUtils.createFile(filepath, data);
            file.newChunk(chunkNo, 1, replicationDeg);
        }
    }
}
