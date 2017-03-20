package protocols;

import communications.Message;
import communications.MessageHeader;
import communications.MessageBody;
import communications.MessageConstants;
import files.FileManager;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class ChunkBackupSubprotocol {
    private FileManager fileManager;
    private String mdbAddr, mcAddr;
    private int mdbPort, mcPort;
    private int serverId;
    
    public ChunkBackupSubprotocol(int serverId, FileManager fileManager, String mdbAddr, int mdbPort, String mcAddr, int mcPort) {
        this.serverId = serverId;
        this.fileManager = fileManager;
        this.mdbAddr = mdbAddr;
        this.mdbPort = mdbPort;
        this.mcAddr = mcAddr;
        this.mcPort = mcPort;
    }

    // TODO: Version nao esta a ser usado para nada...
    // TODO: Nao dar throw destas exception, mas em vez disso lidar com elas
    public void putChunk(float version, int senderId, String fileId, int chunkNo, int replicationDeg, byte[] data) throws UnknownHostException, SocketException, IOException {
        boolean done = false;
        int iteration = 0;
        // TODO: Substituir este magic number por uma constante
        int delay = 1000;

        // TODO: Substituir este magic number por uma constante
        while (!done && iteration < 5) {
            // TODO: Estou a criar uma socket de cada vez que inicio um
            //       putchunk... Se calhar era melhor receber a socket
            //       como argumento?
            InetAddress inetaddress = InetAddress.getByName(mdbAddr);
            DatagramSocket socket = new DatagramSocket(mdbPort, inetaddress);

            MessageHeader header = new MessageHeader()
                .setMessageType(MessageConstants.MessageType.PUTCHUNK)
                .setVersion(version)
                .setSenderId(senderId)
                .setFileId(fileId)
                .setChunkNo(chunkNo)
                .setReplicationDeg(replicationDeg);

            MessageBody body = new MessageBody()
                .setContent(data);

            Message message = new Message()
                .addHeader(header)
                .setBody(body);

            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.send(packet);

            // TODO: Esperar um segundo antes de verificar o replication
            //       degree junto do file manager
            //       ...
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                System.out.println("ChunkBackupSubprotocol error: " + e.toString());
                e.printStackTrace();
            }
            
            int currentReplicationDeg = fileManager.getFile(fileId).getChunk(chunkNo).getReplicationDeg();
            if (currentReplicationDeg >= replicationDeg) {
                done = true;
            } else {
                iteration++;
                delay *= 2;
            }
        }
        
    }

    public void storeChunk(Message message) {
        // TODO: O version da mensagem nao esta a ser utilizado para nada
        //       para ja...
        MessageHeader header = message.getHeaders().get(0);
        float version = header.getVersion();
        int senderId = header.getSenderId();
        String fileId = header.getFileId();
        int chunkNo = header.getChunkNo();
        int replicationDeg = header.getReplicationDeg();
        
        byte[] data = message.getBody().getBytes();

        if (senderId != serverId) {
            try {
                fileManager.saveChunk(fileId, chunkNo, replicationDeg, data);
            } catch (IOException e) {
                System.out.println("ChunkBackupSubprotocol error: " + e.toString());
                e.printStackTrace();
            }
        }

        MessageHeader responseHeader = new MessageHeader()
            .setMessageType(MessageConstants.MessageType.STORED)
            .setVersion(version)
            .setSenderId(serverId)
            .setFileId(fileId)
            .setChunkNo(chunkNo);

        Message response = new Message()
            .addHeader(responseHeader);

        // TODO: Utilizar constantes em vez destes numeros magicos
        int delay = ThreadLocalRandom.current().nextInt(0, 401);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            System.out.println("ChunkBackupSubprotocol error: " + e.toString());
            e.printStackTrace();
        }

        try {
            InetAddress inetaddress = InetAddress.getByName(mcAddr);
            DatagramSocket socket = new DatagramSocket(mcPort, inetaddress);

            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.send(packet);
        } catch (UnknownHostException e) {
            System.out.println("ChunkBackupSubprotocol error: " + e.toString());
            e.printStackTrace();
        } catch (SocketException e) {
            System.out.println("ChunkBackupSubprotocol error: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("ChunkBackupSubprotocol error: " + e.toString());
            e.printStackTrace();
        }
    }
}
