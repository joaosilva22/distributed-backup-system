package files;

import java.util.HashMap;
import java.util.ArrayList;

public class ChunkData {
    private ArrayList<Integer> replicationDeg;
    private int desiredReplicationDeg;

    public ChunkData(int serverId, int desiredReplicationDeg) {
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
}
