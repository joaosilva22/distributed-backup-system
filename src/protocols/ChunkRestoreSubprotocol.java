package protocols;

import main.DistributedBackupService;
import utils.IOUtils;
import communications.MessageHeader;
import communications.Message;
import communications.MessageConstants;
import files.FileManager;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.io.IOException;

public class ChunkRestoreSubprotocol {
    private FileManager fileManager;
    private String mdrAddr, mcAddr;
    private int mdrPort, mcPort;
    private int serverId;
    
    public ChunkRestoreSubprotocol(DistributedBackupService service) {
        serverId = service.getServerId();
        fileManager = service.getFileManager();
        mdrAddr = service.getMdrAddr();
        mdrPort = service.getMdrPort();
        mcAddr = service.getMcAddr();
        mcPort = service.getMcPort();
    }

    public void initGetchunk(float version, int senderId, String fileId, int chunkNo) {
        IOUtils.log("Initiating GETCHUNK <" + fileId + ", " + chunkNo + ">");

        MessageHeader header = new MessageHeader()
            .setMessageType(MessageConstants.MessageType.GETCHUNK)
            .setVersion(version)
            .setSenderId(senderId)
            .setFileId(fileId)
            .setChunkNo(chunkNo);

        Message message = new Message()
            .addHeader(header);

        try {
            InetAddress inetaddress = InetAddress.getByName(mcAddr);
            DatagramSocket socket = new DatagramSocket();
            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, inetaddress, mcPort);
            socket.send(packet);
        } catch (UnknownHostException e) {
            IOUtils.err("ChunkRestoreSubprotocol error: " + e.toString());
            e.printStackTrace();
        } catch (SocketException e) {
            IOUtils.err("ChunkRestoreSubprotocol error: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            IOUtils.err("ChunkRestoreSubprotocol error: " + e.toString());
            e.printStackTrace();
        }
    }

    public void getchunk(Message request) {
        // TODO: O version da mensagem nao esta a ser utilizado para nada
        //       para ja...
        MessageHeader requestHeader = request.getHeaders().get(0);
        String fileId = requestHeader.getFileId();
        float version = requestHeader.getVersion();
        int senderId = requestHeader.getSenderId();
        int chunkNo = requestHeader.getChunkNo();

        IOUtils.log("Received GETCHUNK <" + fileId + ", " + chunkNo + ">");

        if (senderId != serverId) {
            byte[] data = fileManager.retrieveChunkData(fileId, chunkNo);
        }
    }
}
