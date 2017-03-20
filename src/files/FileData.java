package files;

import java.util.HashMap;

public class FileData {
    private HashMap<Integer, ChunkData> chunks;

    public FileData() {
        chunks = new HashMap<>();
    }

    public ChunkData getChunk(Integer chunkNo) {
        return chunks.get(chunkNo);
    }

    public void newChunk(int chunkNo, int replicationDeg, int desiredReplicationDeg) {
        chunks.put(chunkNo, new ChunkData(replicationDeg, desiredReplicationDeg));
    }
}
