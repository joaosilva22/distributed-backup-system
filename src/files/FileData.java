package files;

import java.io.Serializable;
import java.util.HashMap;

public class FileData implements Serializable {
    private FileMetadata metadata;
    private HashMap<Integer, ChunkData> chunks;

    public FileData() {
        metadata = null;
        chunks = new HashMap<>();
    }

    public FileData(FileMetadata metadata) {
        this.metadata = metadata;
        chunks = new HashMap<>();
    }

    public HashMap<Integer, ChunkData> getChunks() {
        return chunks;
    }

    public FileMetadata getMetadata() {
        return metadata;
    }

    public ChunkData getChunk(Integer chunkNo) {
        return chunks.get(chunkNo);
    }

    public void newChunk(int chunkNo, int desiredReplicationDeg) {
        chunks.put(chunkNo, new ChunkData(desiredReplicationDeg));
    }

    public void newChunk(int chunkNo, int serverId, int desiredReplicationDeg) {
        chunks.put(chunkNo, new ChunkData(serverId, desiredReplicationDeg));
    }
}
