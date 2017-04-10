package files;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class FileData implements Serializable {
    private FileMetadata metadata;
    private ConcurrentHashMap<Integer, ChunkData> chunks;

    public FileData() {
        metadata = null;
        chunks = new ConcurrentHashMap<>();
    }

    public FileData(FileMetadata metadata) {
        this.metadata = metadata;
        chunks = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<Integer, ChunkData> getChunks() {
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
