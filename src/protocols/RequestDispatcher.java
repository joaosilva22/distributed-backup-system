package protocols;

import main.DistributedBackupService;
import communications.Message;
import communications.MessageConstants;
import files.FileManager;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestDispatcher extends Thread {
    private ConcurrentLinkedQueue<Message> queue;
    private FileManager fileManager;
    private ChunkBackupSubprotocol chunkBackupSubprotocol;

    public RequestDispatcher(DistributedBackupService service) {
        queue = service.getQueue();
        fileManager = service.getFileManager();
        chunkBackupSubprotocol = service.getChunkBackupSubprotocol();
    }

    public void run() {
        // TODO: Nao pode ser um loop infinito
        while (true) {
            if (!queue.isEmpty()) {
                Message message = queue.poll();
                if (message != null) {
                    switch (message.getMessageType()) {
                        case MessageConstants.MessageType.PUTCHUNK:
                            System.out.println("RECEIVED PUTCHUNK");
                            new Thread(() -> chunkBackupSubprotocol.putchunk(message)).start();
                            break;
                        case MessageConstants.MessageType.STORED:
                            System.out.println("RECEIVED STORED");
                            new Thread(() -> chunkBackupSubprotocol.stored(message)).start();
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
