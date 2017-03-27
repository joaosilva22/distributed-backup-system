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
    private FileDeletionSubprotocol fileDeletionSubprotocol;

    public RequestDispatcher(DistributedBackupService service) {
        queue = service.getQueue();
        fileManager = service.getFileManager();
        chunkBackupSubprotocol = service.getChunkBackupSubprotocol();
        chunkRestoreSubprotocol = service.getChunkRestoreSubprotocol();
        fileDeletionSubprotocol = service.getFileDeletionSubprotocol();
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
                            new Thread(() -> chunkRestoreSubprotocol.chunk(message)).start();
                            break;
                        case MessageConstants.MessageType.DELETE:
                            new Thread(() -> fileDeletionSubprotocol.delete(message)).start();
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
