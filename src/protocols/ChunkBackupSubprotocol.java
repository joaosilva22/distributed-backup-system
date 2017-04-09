package protocols;

import main.DistributedBackupService;
import communications.Message;
import communications.MessageBody;
import communications.MessageHeader;
import communications.MessageConstants;
import files.FileManager;
import utils.IOUtils;
import utils.Tuple;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class ChunkBackupSubprotocol {
    private FileManager fileManager;
    private String mdbAddr, mcAddr;
    private int mdbPort, mcPort;
    private int serverId;

    private Vector<Tuple<String, Integer>> outgoing;

    public ChunkBackupSubprotocol(DistributedBackupService service) {
        serverId = service.getServerId();
        fileManager = service.getFileManager();
        mdbAddr = service.getMdbAddr();
        mdbPort = service.getMdbPort();
        mcAddr = service.getMcAddr();
        mcPort = service.getMcPort();

        outgoing = new Vector<>();
    }

    public void initPutchunk(float version, int senderId, String fileId, int chunkNo, int replicationDeg, byte[] data) {
        IOUtils.log("Initiating PUTCHUNK <" + fileId + ", " + chunkNo + ">");
        boolean done = false;
        int iteration = 0;
        // TODO: Substituir este magic number por uma constante
        int delay = 1000;

        // TODO: Substituir este magic number por uma constante
        while (!done && iteration < 5) {
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

            try {
                InetAddress inetaddress = InetAddress.getByName(mdbAddr);
                DatagramSocket socket = new DatagramSocket();
                byte[] buf = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, inetaddress, mdbPort);
                socket.send(packet);
            } catch (UnknownHostException e) {
                IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                e.printStackTrace();
            } catch (SocketException e) {
                IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                e.printStackTrace();
            } catch (IOException e) {
                IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                e.printStackTrace();
            }

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                e.printStackTrace();
            }
            
            int currentReplicationDeg = fileManager.getChunkReplicationDegree(fileId, chunkNo);
            if (currentReplicationDeg >= replicationDeg) {
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

        try {
            fileManager.save(serverId);
        } catch (IOException e) {
            IOUtils.warn("ChunkBackupSubprotocol warning: Failed to save metadata " + e.toString());
            e.printStackTrace();
        }
    }

    public void putchunk(Message request) {
        // TODO: O version da mensagem nao esta a ser utilizado para nada
        //       para ja...
        MessageHeader requestHeader = request.getHeaders().get(0);
        String fileId = requestHeader.getFileId();
        float version = requestHeader.getVersion();
        int senderId = requestHeader.getSenderId();
        int chunkNo = requestHeader.getChunkNo();
        int replicationDeg = requestHeader.getReplicationDeg();
        byte[] data = request.getBody().getBytes();

        IOUtils.log("Received PUTCHUNK <" + fileId + ", " + chunkNo + ">");

        if (senderId != serverId) {
            if (data != null) {
                if (fileManager.getAvailableSpace() < data.length) {
                    IOUtils.warn("ChunkBackupSubprotocol warning: Not enough space <" + fileId + ", " + chunkNo + ">");
                    return;
                }

                if (fileManager.getAvailableSpace() < fileManager.getStorageSpace() / 2) {
                    ArrayList<Tuple<String, Integer>> chunksToRemove = fileManager.getChunksWithReplicationDegreeTooDamnHigh();
                    for (Tuple<String, Integer> chunkToRemove : chunksToRemove) {
                        String fileIdToRemove = chunkToRemove.x;
                        int chunkNoToRemove = chunkToRemove.y;
                        IOUtils.log("Deleting chunk to free space <" + fileIdToRemove + ", " + chunkNoToRemove + ">");
                        fileManager.deleteChunkFile(fileIdToRemove, chunkNoToRemove);
                        fileManager.addUsedSpace(fileManager.getChunk(fileIdToRemove, chunkNoToRemove).getSize() * -1);
                        fileManager.deleteChunk(fileIdToRemove, chunkNoToRemove);

                        MessageHeader removedHeader = new MessageHeader()
                            .setMessageType(MessageConstants.MessageType.REMOVED)
                            .setVersion(version)
                            .setSenderId(serverId)
                            .setFileId(fileIdToRemove)
                            .setChunkNo(chunkNoToRemove);

                        Message removed = new Message()
                            .addHeader(removedHeader);

                        try {
                            InetAddress inetaddress = InetAddress.getByName(mcAddr);
                            DatagramSocket socket = new DatagramSocket();
                            byte[] buf = removed.getBytes();
                            DatagramPacket packet = new DatagramPacket(buf, buf.length, inetaddress, mcPort);
                            socket.send(packet);
                        } catch (UnknownHostException e) {
                            IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                            e.printStackTrace();
                        } catch (SocketException e) {
                            IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                            e.printStackTrace();
                        } catch (IOException e) {
                            IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                            e.printStackTrace();
                        }
                    }
                }
            }            
            try {
                fileManager.saveChunk(serverId, fileId, chunkNo, replicationDeg, data);
            } catch (IOException e) {
                IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                e.printStackTrace();
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
                IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                e.printStackTrace();
            }

            try {
                InetAddress inetaddress = InetAddress.getByName(mcAddr);
                DatagramSocket socket = new DatagramSocket();
                byte[] buf = response.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, inetaddress, mcPort);
                socket.send(packet);
            } catch (UnknownHostException e) {
                IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                e.printStackTrace();
            } catch (SocketException e) {
                IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                e.printStackTrace();
            } catch (IOException e) {
                IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                e.printStackTrace();
            }
        }
        try {
            fileManager.save(serverId);
        } catch (IOException e) {
            IOUtils.warn("ChunkBackupSubprotocol warning: Failed to save metadata" + e.toString());
            e.printStackTrace();
        }
    }

    public void enhancedPutchunk(Message request) {
        // TODO: O version da mensagem nao esta a ser utilizado para nada
        //       para ja...
        MessageHeader requestHeader = request.getHeaders().get(0);
        String fileId = requestHeader.getFileId();
        float version = requestHeader.getVersion();
        int senderId = requestHeader.getSenderId();
        int chunkNo = requestHeader.getChunkNo();
        int replicationDeg = requestHeader.getReplicationDeg();
        byte[] data = request.getBody().getBytes();

        IOUtils.log("Received (enhanced) PUTCHUNK <" + fileId + ", " + chunkNo + ">");

        if (senderId != serverId) {
            if (data != null) {
                if (fileManager.getAvailableSpace() < data.length) {
                    IOUtils.warn("ChunkBackupSubprotocol warning: Not enough space <" + fileId + ", " + chunkNo + ">");
                    return;
                }

                if (fileManager.getAvailableSpace() < fileManager.getStorageSpace() / 2) {
                    ArrayList<Tuple<String, Integer>> chunksToRemove = fileManager.getChunksWithReplicationDegreeTooDamnHigh();
                    for (Tuple<String, Integer> chunkToRemove : chunksToRemove) {
                        String fileIdToRemove = chunkToRemove.x;
                        int chunkNoToRemove = chunkToRemove.y;
                        IOUtils.log("Deleting chunk to free space <" + fileIdToRemove + ", " + chunkNoToRemove + ">");
                        fileManager.deleteChunkFile(fileIdToRemove, chunkNoToRemove);
                        fileManager.addUsedSpace(fileManager.getChunk(fileIdToRemove, chunkNoToRemove).getSize() * -1);
                        fileManager.deleteChunk(fileIdToRemove, chunkNoToRemove);

                        MessageHeader removedHeader = new MessageHeader()
                            .setMessageType(MessageConstants.MessageType.REMOVED)
                            .setVersion(version)
                            .setSenderId(serverId)
                            .setFileId(fileIdToRemove)
                            .setChunkNo(chunkNoToRemove);

                        Message removed = new Message()
                            .addHeader(removedHeader);

                        try {
                            InetAddress inetaddress = InetAddress.getByName(mcAddr);
                            DatagramSocket socket = new DatagramSocket();
                            byte[] buf = removed.getBytes();
                            DatagramPacket packet = new DatagramPacket(buf, buf.length, inetaddress, mcPort);
                            socket.send(packet);
                        } catch (UnknownHostException e) {
                            IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                            e.printStackTrace();
                        } catch (SocketException e) {
                            IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                            e.printStackTrace();
                        } catch (IOException e) {
                            IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                            e.printStackTrace();
                        }
                    }
                }
            }            
            try {
                fileManager.saveChunk(serverId, fileId, chunkNo, replicationDeg, data);
                outgoing.add(new Tuple<fileId, chunkNo>);
            } catch (IOException e) {
                IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                e.printStackTrace();
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
                IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                e.printStackTrace();
            }

            if (outgoing.contains(new Tuple<fileId, chunkNo>)) {
                outgoing.remove(new Tuple<fileId, chunkNo>);s
                try {
                    InetAddress inetaddress = InetAddress.getByName(mcAddr);
                    DatagramSocket socket = new DatagramSocket();
                    byte[] buf = response.getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, inetaddress, mcPort);
                    socket.send(packet);
                } catch (UnknownHostException e) {
                    IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                    e.printStackTrace();
                } catch (SocketException e) {
                    IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                    e.printStackTrace();
                } catch (IOException e) {
                    IOUtils.err("ChunkBackupSubprotocol error: " + e.toString());
                    e.printStackTrace();
                }
            } else {
                fileManager.deleteChunkFile(fileId, chunkNo);
                fileManager.deleteChunk(fileId, chunkNo);
            }
        }
        try {
            fileManager.save(serverId);
        } catch (IOException e) {
            IOUtils.warn("ChunkBackupSubprotocol warning: Failed to save metadata" + e.toString());
            e.printStackTrace();
        }
    }    

    public void stored(Message request) {
        MessageHeader requestHeader = request.getHeaders().get(0);

        String fileId = requestHeader.getFileId();
        // TODO: O version da mensagem nao esta a ser utilizado para nada
        //       para ja...
        // float version = requestHeader.getVersion();
        int senderId = requestHeader.getSenderId();
        int chunkNo = requestHeader.getChunkNo();

        IOUtils.log("Received STORED <" + fileId + ", " + chunkNo + ">");

        fileManager.incrementReplicationDeg(senderId, fileId, chunkNo);
        try {
            fileManager.save(serverId);
        } catch (IOException e) {
            IOUtils.warn("ChunkBackupSubprotocol warning: Failed to save metadata" + e.toString());
            e.printStackTrace();
        }
    }

    public void enhancedStored(Message request) {
        MessageHeader requestHeader = request.getHeaders().get(0);

        String fileId = requestHeader.getFileId();
        int senderId = requestHeader.getSenderId();
        int chunkNo = requestHeader.getChunkNo();

        IOUtils.log("Received (enhanced) STORED <" + fileId + ", " + chunkNo + ">");

        fileManager.incrementReplicationDeg(senderId, fileId, chunkNo);
        if (outgoing.contains(new Tuple<>(fileId, chunkNo))) {
            replicationDegree = fileManager.getChunkReplicationDegree(fileId, chunkNo);
            desiredReplicationDegree = fileManager.getChunkDesiredReplicationDegree(fileId, chunkNo);
            if (replicationDegree >= desiredReplicationDegree) {
                outgoing.remove(new Tuple<>(fileId, chunkNo));
            }
        }
        try {
            fileManager.save(serverId);
        } catch (IOException e) {
            IOUtils.warn("ChunkBackupSubprotocol warning: Failed to save metadata" + e.toString());
            e.printStackTrace();
        }
    }
}
