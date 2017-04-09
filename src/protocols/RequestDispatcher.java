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
    private SpaceReclaimingSubprotocol spaceReclaimingSubprotocol;

    public RequestDispatcher(DistributedBackupService service) {
        queue = service.getQueue();
        fileManager = service.getFileManager();
        chunkBackupSubprotocol = service.getChunkBackupSubprotocol();
        chunkRestoreSubprotocol = service.getChunkRestoreSubprotocol();
        fileDeletionSubprotocol = service.getFileDeletionSubprotocol();
        spaceReclaimingSubprotocol = service.getSpaceReclaimingSubprotocol();
    }

    public void run() {
        // TODO: Nao pode ser um loop infinito
        while (true) {
            if (!queue.isEmpty()) {
                Message message = queue.poll();
                float version = message.getHeaders().get(0).getVersion();
                if (message != null) {
                    switch (message.getMessageType()) {
                        case MessageConstants.MessageType.PUTCHUNK:
                            if (version == 1.0f) {
                                new Thread(() -> chunkBackupSubprotocol.putchunk(message)).start();
                            }
                            if (version == 1.1f) {
                                new Thread(() -> chunkBackupSubprotocol.enhancedPutchunk(message)).start();
                            }
                            break;
                        case MessageConstants.MessageType.STORED:
                            if (version == 1.0f) {
                                new Thread(() -> chunkBackupSubprotocol.stored(message)).start();
                            }
                            if (version == 1.1f) {
                                new Thread(() -> chunkBackupSubprotocol.enhancedStored(message)).start();
                            }
                            break;
                        case MessageConstants.MessageType.GETCHUNK:
                            if (version == 1.0f) {
                                new Thread(() -> chunkRestoreSubprotocol.getchunk(message)).start();
                            }
                            if (version == 1.1f) {
                                new Thread(() -> chunkRestoreSubprotocol.enhancedGetchunk(message)).start();
                            }
                            break;
                        case MessageConstants.MessageType.CHUNK:
                            if (version == 1.0f) {
                                new Thread(() -> chunkRestoreSubprotocol.chunk(message)).start();
                            }
                            if (version == 1.1f) {
                                new Thread(() -> chunkRestoreSubprotocol.enchancedChunk(message)).start();
                            }
                            break;
                        case MessageConstants.MessageType.DELETE:
                            new Thread(() -> fileDeletionSubprotocol.delete(message)).start();
                            break;
                        case MessageConstants.MessageType.REMOVED:
                            if (version == 1.0f) {
                                new Thread(() -> spaceReclaimingSubprotocol.removed(message)).start();
                            }
                            if (version == 1.1f) {
                                new Thread(() -> spaceReclaimingSubprotocol.enhancedRemoved(message)).start();
                            }
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
