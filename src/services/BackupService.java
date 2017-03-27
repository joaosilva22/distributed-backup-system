package services;

import main.DistributedBackupService;
import utils.FileUtils;
import utils.IOUtils;
import protocols.ChunkBackupSubprotocol;
import protocols.ChunkRestoreSubprotocol;
import protocols.FileDeletionSubprotocol;
import files.FileManager;
import files.FileMetadata;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.io.IOException;

public class BackupService extends UnicastRemoteObject implements BackupServiceInterface {
    private int serverId;
    private FileManager fileManager;
    private ChunkBackupSubprotocol chunkBackupSubprotocol;
    private ChunkRestoreSubprotocol chunkRestoreSubprotocol;
    private FileDeletionSubprotocol fileDeletionSubprotocol;
    
    public BackupService(DistributedBackupService service) throws RemoteException {
        chunkBackupSubprotocol = service.getChunkBackupSubprotocol();
        chunkRestoreSubprotocol = service.getChunkRestoreSubprotocol();
        fileDeletionSubprotocol = service.getFileDeletionSubprotocol();
        serverId = service.getServerId();
        fileManager = service.getFileManager();
    }

    public void backupFile(String filepath, int replicationDeg) {
        String fileId = FileUtils.getFileId(filepath);
        ArrayList<byte[]> chunks = FileUtils.getFileChunks(filepath, BackupServiceConstants.CHUNK_SIZE);

        fileManager.registerFile(filepath, fileId);
        for (int i = 0; i < chunks.size(); i++) {
            final int chunkNo = i;
            final byte[] chunk = chunks.get(i);
            fileManager.registerChunk(fileId, chunkNo, replicationDeg);
            // TODO: Provavelmente aqui a versao nao devia ser uma constante
            //       mas por outro lado nao sei o que devia ser
            new Thread(() -> chunkBackupSubprotocol.initPutchunk(1.0f, serverId, fileId, chunkNo, replicationDeg, chunk)).start();
        }
    }

    public void restoreFile(String filepath) {
        FileMetadata metadata = fileManager.getFileMetadata(filepath);
        if (metadata == null) {
            IOUtils.err("BackupService error: Can't backup file " + filepath);
            return;
        }
        String fileId = metadata.getFileId();
        int numberOfChunks = fileManager.getFile(fileId).getChunks().size();
        
        int iteration = 0;
        int delay = 1000;
        boolean done = false;
        while (!done && iteration < 5) {
            for (int i = 0; i < numberOfChunks; i++) {
                if (fileManager.getFile(fileId).getChunk(i).getData() == null) {
                    final int chunkNo = i;
                    new Thread(() -> chunkRestoreSubprotocol.initGetchunk(1.0f, serverId, fileId, chunkNo)).start();
                }
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                IOUtils.err("BackupService error: " + e.toString());
                e.printStackTrace();
            }
            done = true;
            for (int i = 0; i < numberOfChunks; i++) {
                if (fileManager.getFile(fileId).getChunk(i).getData() == null) {
                    done = false;
                }
            }
            if (!done) {
                iteration += 1;
                delay *= 2;
                if (iteration < 5) {
                    IOUtils.warn("Failed to get all necessary chunks to recover file '" + filepath + "', retrying ");
                } else {
                    IOUtils.warn("Failed to get all necessary chunks to recover file '" + filepath + "', aborting ");
                }
            }
        }        
        if (done) {
            try {
                FileUtils.createFile(filepath, null);
                for (int i = 0; i < numberOfChunks; i++) {
                    byte[] data = fileManager.getFile(fileId).getChunk(i).getData();
                    FileUtils.writeToFile(filepath, data);
                }
                IOUtils.log("Recovered file " + filepath);
            } catch (IOException e) {
                IOUtils.err("BackupService error: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    public void deleteFile(String filepath) {
        String fileId = FileUtils.getFileId(filepath);
        new Thread(() -> fileDeletionSubprotocol.initDelete(1.0f, serverId, fileId)).start();
    }

    // TODO: Adicionar os metodos relativos aos restantes protocolos
    //       ...
}
