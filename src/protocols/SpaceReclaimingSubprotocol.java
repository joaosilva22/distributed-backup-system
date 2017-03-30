package protocols;

import main.DistributedBackupService;
import files.FileManager;
import utils.Tuple;
import utils.IOUtils;
import communications.Message;
import communications.MessageBody;
import communications.MessageHeader;
import communications.MessageConstants;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

public class SpaceReclaimingSubprotocol {
    private FileManager fileManager;
    private String mcAddr;
    private int mcPort;
    private int serverId;

    public Vector<Tuple<String, Integer>> outgoing;
    
    public SpaceReclaimingSubprotocol(DistributedBackupService service) {
        serverId = service.getServerId();
        fileManager = service.getFileManager();
        mcAddr = service.getMcAddr();
        mcPort = service.getMcPort();

        outgoing = new Vector<>();
    }

    public void removed(Message request) {
        MessageHeader requestHeader = request.getHeaders().get(0);
        float version = requestHeader.getVersion();
        int senderId = requestHeader.getSenderId();
        String fileId = requestHeader.getFileId();
        int chunkNo = requestHeader.getChunkNo();

        IOUtils.log("Received REMOVED <" + fileId + ", " + chunkNo + ">");

        if (senderId != serverId) {        
            if (fileManager.getChunk(fileId, chunkNo) != null) {
                fileManager.decreaseReplicationDegree(senderId, fileId, chunkNo);            
                int replicationDegree = fileManager.getChunkReplicationDegree(fileId, chunkNo);
                int desiredReplicationDegree = fileManager.getChunkDesiredReplicationDegree(fileId, chunkNo);
            
                if (replicationDegree < desiredReplicationDegree) {
                    outgoing.add(new Tuple<>(fileId, chunkNo));
                    int delay = ThreadLocalRandom.current().nextInt(0, 401); // TODO: Isto devia ser uma constante
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        IOUtils.err("SpaceReclaimingSubprotocol error: " + e.toString());
                        e.printStackTrace();
                    }
                    if (!outgoing.contains(new Tuple<>(fileId, chunkNo))) {
                        return;
                    }
                
                    byte[] data = fileManager.retrieveChunkData(fileId, chunkNo);
                    boolean done = false;
                    int iteration = 0;
                    delay = 1000; // TODO: Isto devia ser uma constante

                    while (!done && iteration < 5) {
                        MessageHeader header = new MessageHeader()
                            .setMessageType(MessageConstants.MessageType.PUTCHUNK)
                            .setVersion(version)
                            .setSenderId(serverId)
                            .setFileId(fileId)
                            .setChunkNo(chunkNo)
                            .setReplicationDeg(desiredReplicationDegree);

                        MessageBody body = new MessageBody()
                            .setContent(data);

                        Message message = new Message()
                            .addHeader(header)
                            .setBody(body);

                        try {
                            InetAddress inetaddress = InetAddress.getByName(mcAddr);
                            DatagramSocket socket = new DatagramSocket();
                            byte[] buf = message.getBytes();
                            DatagramPacket packet = new DatagramPacket(buf, buf.length, inetaddress, mcPort);
                            socket.send(packet);
                        } catch (UnknownHostException e) {
                            IOUtils.err("SpaceReclaimingSubprotocol error: " + e.toString());
                            e.printStackTrace();
                        } catch (SocketException e) {
                            IOUtils.err("SpaceReclaimingSubprotocol error: " + e.toString());
                            e.printStackTrace();
                        } catch (IOException e) {
                            IOUtils.err("SpaceReclaimingSubprotocol error: " + e.toString());
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            IOUtils.err("SpaceReclaimingSubprotocol error: " + e.toString());
                            e.printStackTrace();
                        }

                        replicationDegree = fileManager.getChunkReplicationDegree(fileId, chunkNo);
                        desiredReplicationDegree = fileManager.getChunkDesiredReplicationDegree(fileId, chunkNo);
                        if (replicationDegree >= desiredReplicationDegree) {
                            done = true;
                        } else {
                            iteration += 1;
                            delay *= 2;
                            if (iteration < 5) {
                                IOUtils.warn("Failed to hit target replication deg, retrying <" + fileId + ", " + chunkNo + ">");
                            } else {
                                IOUtils.warn("Failed to hit target replication deg, aborting <" + fileId + ", " + chunkNo + ">");
                            }
                        }
                    }
                    if (done) {
                        IOUtils.log("Successfully stored <" + fileId + ", " + chunkNo + ">");
                    }
                }
            }
        }
        try {
            fileManager.save(serverId);
        } catch (IOException e) {
            IOUtils.warn("SpaceReclaimingSubprotocol warning: Failed to save metadata " + e.toString());
            e.printStackTrace();
        }
    }

    public void putchunk(Message request) {
        MessageHeader requestHeader = request.getHeaders().get(0);
        String fileId = requestHeader.getFileId();
        int chunkNo = requestHeader.getChunkNo();

        Tuple chunk = new Tuple<>(fileId, chunkNo);
        if (outgoing.contains(chunk)) {
            outgoing.remove(chunk);
        }
    }
}
