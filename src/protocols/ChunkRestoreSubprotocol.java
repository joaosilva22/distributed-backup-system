package protocols;

import main.DistributedBackupService;
import utils.IOUtils;
import utils.Tuple;
import communications.Message;
import communications.MessageBody;
import communications.MessageHeader;
import communications.MessageConstants;
import files.FileManager;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

public class ChunkRestoreSubprotocol {
    private FileManager fileManager;
    private String mdrAddr, mcAddr;
    private int mdrPort, mcPort;
    private int serverId;

    private Vector<Tuple<String, Integer>> incoming;
    private Vector<Tuple<String, Integer>> outgoing;
    
    public ChunkRestoreSubprotocol(DistributedBackupService service) {
        serverId = service.getServerId();
        fileManager = service.getFileManager();
        mdrAddr = service.getMdrAddr();
        mdrPort = service.getMdrPort();
        mcAddr = service.getMcAddr();
        mcPort = service.getMcPort();
        
        incoming = new Vector<>();
        outgoing = new Vector<>();
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

        if (!incoming.contains(new Tuple<>(fileId, chunkNo))) {
            incoming.add(new Tuple<>(fileId, chunkNo));
        }

        try {
            fileManager.save(serverId);
        } catch (IOException e) {
            IOUtils.warn("ChunkRestoreSubprotocol warning: Failed to save metadata" + e.toString());
            e.printStackTrace();
        }
    }

    public void getchunk(Message request) {
        // TODO: A version da mensagem nao esta a ser utilizado para nada
        //       para ja...
        MessageHeader requestHeader = request.getHeaders().get(0);
        String fileId = requestHeader.getFileId();
        float version = requestHeader.getVersion();
        int senderId = requestHeader.getSenderId();
        int chunkNo = requestHeader.getChunkNo();

        IOUtils.log("Received GETCHUNK <" + fileId + ", " + chunkNo + ">");

        if (senderId != serverId) {
            byte[] data = fileManager.retrieveChunkData(fileId, chunkNo);
            if (data != null) {
                MessageHeader header = new MessageHeader()
                    .setMessageType(MessageConstants.MessageType.CHUNK)
                    .setVersion(version)
                    .setSenderId(serverId)
                    .setFileId(fileId)
                    .setChunkNo(chunkNo);

                MessageBody body = new MessageBody()
                    .setContent(data);

                Message message = new Message()
                    .addHeader(header)
                    .setBody(body);

                outgoing.add(new Tuple<>(fileId, chunkNo));
                // TODO: Subsituir isto por uma constante
                int delay = ThreadLocalRandom.current().nextInt(0, 401);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    IOUtils.err("ChunkRestoreSubprotocol error: " + e.toString());
                    e.printStackTrace();
                }
                if (!outgoing.contains(new Tuple<>(fileId, chunkNo))) {
                    return;
                }
                outgoing.remove(new Tuple<>(fileId, chunkNo));

                try {
                    // TODO: Estou a criar uma socket de cada vez que inicio um
                    //       putchunk... Se calhar era melhor receber a socket
                    //       como argumento?
                    InetAddress inetaddress = InetAddress.getByName(mdrAddr);
                    DatagramSocket socket = new DatagramSocket();
                    byte[] buf = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, inetaddress, mdrPort);
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
        }

        try {
            fileManager.save(serverId);
        } catch (IOException e) {
            IOUtils.warn("ChunkRestoreSubprotocol warning: Failed to save metadata" + e.toString());
            e.printStackTrace();
        }
    }

    public void chunk(Message request) {
        MessageHeader requestHeader = request.getHeaders().get(0);
        String fileId = requestHeader.getFileId();
        // TODO: Version não está a ser usado
        // float version = requestHeader.getVersion();
        int chunkNo = requestHeader.getChunkNo();
        byte[] data = request.getBody().getBytes();
        
        IOUtils.log("Received CHUNK <" + fileId + ", " + chunkNo + ">");

        if (outgoing.contains(new Tuple<>(fileId, chunkNo))) {
            outgoing.remove(new Tuple<>(fileId, chunkNo));
        }
        if (incoming.contains(new Tuple<>(fileId, chunkNo))) {
            fileManager.recoverChunk(fileId, chunkNo, data);
            incoming.remove(new Tuple<>(fileId, chunkNo));
        }

        try {
            fileManager.save(serverId);
        } catch (IOException e) {
            IOUtils.warn("ChunkRestoreSubprotocol warning: Failed to save metadata" + e.toString());
            e.printStackTrace();
        }
    }
}
