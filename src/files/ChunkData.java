package files;

public class ChunkData {
    private int replicationDeg;
    private int desiredReplicationDeg;

    public ChunkData(int replicationDeg, int desiredReplicationDeg) {
        this.replicationDeg = replicationDeg;
        this.desiredReplicationDeg = desiredReplicationDeg;
    }

    public int getReplicationDeg() {
        return replicationDeg;
    }
}
