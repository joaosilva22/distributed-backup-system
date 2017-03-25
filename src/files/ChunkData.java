package files;

import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChunkData implements Serializable {
    private ArrayList<Integer> replicationDeg;
    private int desiredReplicationDeg;
    private AtomicBoolean expected;
    private ArrayList<Byte> data;
    private int size;

    public ChunkData(int desiredReplicationDeg) {
        replicationDeg = new ArrayList<>();
        this.desiredReplicationDeg = desiredReplicationDeg;
        expected = new AtomicBoolean(false);
        data = new ArrayList<>();
        size = 0;
    }

    public ChunkData(int serverId, int desiredReplicationDeg) {
        replicationDeg = new ArrayList<>();
        this.replicationDeg.add(serverId);
        this.desiredReplicationDeg = desiredReplicationDeg;
    }

    public int getReplicationDeg() {
        return replicationDeg.size();
    }

    public void incrementReplicationDeg(int serverId) {
        if (!replicationDeg.contains(serverId)) {
            replicationDeg.add(serverId);
        }
    }

    public void setSize(int size) {
        this.size = size;
    }
}
