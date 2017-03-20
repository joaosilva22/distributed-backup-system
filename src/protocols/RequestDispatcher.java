package protocols;

import communications.Message;
import communications.MessageConstants;
import files.FileManager;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestDispatcher extends Thread {
    private ConcurrentLinkedQueue<Message> queue;
    private FileManager fileManager;
    private ChunkBackupSubprotocol chunkBackupSubprotocol;
    
    public RequestDispatcher(int serverId, ConcurrentLinkedQueue<Message> queue, String mdbAddr, int mdbPort, String mcAddr, int mcPort) {
        this.queue = queue;
        fileManager = new FileManager();
        chunkBackupSubprotocol = new ChunkBackupSubprotocol(serverId, fileManager, mdbAddr, mdbPort, mcAddr, mcPort);
    }

    public void run() {
        // TODO: Nao pode ser um loop infinito
        while (true) {
            if (!queue.isEmpty()) {
                Message message = queue.poll();
                if (message != null) {
                    switch (message.getMessageType()) {
                        case MessageConstants.MessageType.PUTCHUNK:
                            new Thread(() -> chunkBackupSubprotocol.putchunk(message)).start();
                            break;
                        default:
                            System.out.println("RequestDispatcher error: Unknown message type");
                            break;
                    }
                }
            }
        }
    }
}
