package protocols;

import main.DistributedBackupService;
import utils.IOUtils;
import communications.MessageHeader;
import communications.Message;
import communications.MessageConstants;
import files.FileManager;
import files.FileData;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.io.IOException;

public class FileDeletionSubprotocol {
    private FileManager fileManager;
    private String mcAddr;
    private int mcPort;
    private int serverId;
    
    public FileDeletionSubprotocol(DistributedBackupService service) {
        serverId = service.getServerId();
        fileManager = service.getFileManager();
        mcAddr = service.getMcAddr();
        mcPort = service.getMcPort();
    }

    public void initDelete(float version, int senderId, String fileId) {
        IOUtils.log("Initiating DELETE <" + fileId + ">");
        fileManager.deleteFile(fileId);

        MessageHeader header = new MessageHeader()
            .setMessageType(MessageConstants.MessageType.DELETE)
            .setVersion(version)
            .setSenderId(senderId)
            .setFileId(fileId);

        Message message = new Message()
            .addHeader(header);

        try {
            InetAddress inetaddress = InetAddress.getByName(mcAddr);
            DatagramSocket socket = new DatagramSocket();
            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, inetaddress, mcPort);
            socket.send(packet);
        } catch (UnknownHostException e) {
            IOUtils.err("FileDeletionSubprotocol error: " + e.toString());
            e.printStackTrace();
        } catch (SocketException e) {
            IOUtils.err("FileDeletionSubprotocol error: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            IOUtils.err("FileDeletionSubprotocol error: " + e.toString());
            e.printStackTrace();
        }

        try {
            fileManager.save(serverId);
        } catch (IOException e) {
            IOUtils.warn("FileDeletionSubprotocol warning: Failed to save metadata" + e.toString());
            e.printStackTrace();
        }
    }

    public void delete(Message request) {
        MessageHeader requestHeader = request.getHeaders().get(0);
        String fileId = requestHeader.getFileId();
        float version = requestHeader.getVersion();
        int senderId = requestHeader.getSenderId();

        IOUtils.log("Received DELETE <" + fileId + ">");

        FileData file = fileManager.getFile(fileId);
        if (file != null && file.getMetadata() == null) {
            for (int chunkNo : file.getChunks().keySet()) {
                System.out.println("AVAILABLE SPACE=" + fileManager.getAvailableSpace());
                IOUtils.log("Deleted file " + fileId);
                fileManager.deleteChunkFile(fileId, chunkNo);
                fileManager.addUsedSpace(file.getChunk(chunkNo).getSize() * -1);
                System.out.println("AVAILABLE SPACE=" + fileManager.getAvailableSpace());
            }
            fileManager.deleteFile(fileId);
        }

        try {
            fileManager.save(serverId);
        } catch (IOException e) {
            IOUtils.warn("FileDeletionSubprotocol warning: Failed to save metadata" + e.toString());
            e.printStackTrace();
        }
    }
}
