package files;

import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;

public class ChunkData implements Serializable {
    private ArrayList<Integer> replicationDeg;
    private int desiredReplicationDeg;
    private byte[] data;
    private int size;

    public ChunkData() {}

    public ChunkData(int desiredReplicationDeg) {
        replicationDeg = new ArrayList<>();
        this.desiredReplicationDeg = desiredReplicationDeg;
        data = null;
        size = 0;
    }

    public ChunkData(int serverId, int desiredReplicationDeg) {
        replicationDeg = new ArrayList<>();
        this.replicationDeg.add(serverId);
        this.desiredReplicationDeg = desiredReplicationDeg;
        data = null;
        size = 0;
    }

    public void incrementReplicationDegree(int serverId) {
        if (!replicationDeg.contains(serverId)) {
            replicationDeg.add(serverId);
        }
    }

    public void decreaseReplicationDegree(int serverId) {
        if (replicationDeg.contains(serverId)) {
            replicationDeg.remove(new Integer(serverId));
        }
    }

    public int getReplicationDegree() {
        return replicationDeg.size();
    }

    public void setDesiredReplicationDegree(int degree) {
        desiredReplicationDeg = degree;
    }

    public int getDesiredReplicationDegree() {
        return desiredReplicationDeg;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setData(byte[] data) {
        if (data == null) {
            this.data = new byte[0];
            return;
        }
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
