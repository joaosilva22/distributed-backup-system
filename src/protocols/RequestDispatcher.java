package protocols;

import main.DistributedBackupService;
import communications.Message;
import communications.MessageConstants;
import files.FileManager;
import utils.IOUtils;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestDispatcher extends Thread {
    private ConcurrentLinkedQueue<Message> queue;
    private FileManager fileManager;
    private ChunkBackupSubprotocol chunkBackupSubprotocol;
    private ChunkRestoreSubprotocol chunkRestoreSubprotocol;

    public RequestDispatcher(DistributedBackupService service) {
        queue = service.getQueue();
        fileManager = service.getFileManager();
        chunkBackupSubprotocol = service.getChunkBackupSubprotocol();
        chunkRestoreSubprotocol = service.getChunkRestoreSubprotocol();
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
                        case MessageConstants.MessageType.STORED:
                            new Thread(() -> chunkBackupSubprotocol.stored(message)).start();
                            break;
                        case MessageConstants.MessageType.GETCHUNK:
                            new Thread(() -> chunkRestoreSubprotocol.getchunk(message)).start();
                            break;
                        case MessageConstants.MessageType.CHUNK:
                            System.out.println("CHHHUUUUUUUUUUUUUUNK");
                            break;
                        default:
                            IOUtils.err("RequestDispatcher error: Unknown message type");
                            break;
                    }
                }
            }
        }
    }
}
