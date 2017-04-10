package protocols;

import main.DistributedBackupService;
import files.FileManager;
import files.FileData;
import files.ChunkData;
import utils.IOUtils;

import java.util.TimerTask;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ExtendLeaseTask extends TimerTask {
    private FileManager fileManager;
    private FileDeletionSubprotocol fileDeletionSubprotocol;
    private int serverId;
    
    public ExtendLeaseTask(DistributedBackupService service) {
        fileManager = service.getFileManager();
        fileDeletionSubprotocol = service.getFileDeletionSubprotocol();
        serverId = service.getServerId();
    }
    
    public void run() {
        HashMap<String, FileData> files = fileManager.getStoredChunks();
        for (String fileId : files.keySet()) {
            ConcurrentHashMap<Integer, ChunkData> chunks = fileManager.getFileChunks(fileId);
            for (int chunkNo : chunks.keySet()) {
                if (fileManager.hasChunkExpired(fileId, chunkNo)) {
                    new Thread(() -> fileDeletionSubprotocol.initGetlease(1.1f, serverId, fileId, chunkNo)).start();
                }
            }
        }
    }
}
