package protocol;

import communication.Message;
import communication.MessageHeader;
import communication.MessageBody;

import java.net.InetAddress;
import java.net.DatagramSocket;

public class ChunkBackupSubprotocol {
    public static void putChunk(String address, int port, float version, int senderId, String fileId, int chunkNo, int replicationDeg, byte[] data) {
        boolean done = false;
        int iteration = 0;
        
        while (!done && iteration < 5) {
            // TODO: Estou a criar uma socket de cada vez que inicio um
            //       putchunk... Se calhar era melhor receber a socket
            //       como argumento ?
            InetAddress address = InetAddress.getbyName(address);
            DatagramSocket socket = new DatagramSocket(port, address);

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
        }
        
    }

    public static void storeChunk(Message message, String address, int port) {
    }
}
