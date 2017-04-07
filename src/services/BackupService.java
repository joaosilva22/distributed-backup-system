package services;

import main.DistributedBackupService;
import utils.FileUtils;
import utils.IOUtils;
import protocols.ChunkBackupSubprotocol;
import protocols.ChunkRestoreSubprotocol;
import protocols.FileDeletionSubprotocol;
import protocols.SpaceReclaimingSubprotocol;
import files.FileManager;
import files.FileMetadata;
import files.FileData;
import files.ChunkData;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;

public class BackupService extends UnicastRemoteObject implements BackupServiceInterface {
    private int serverId;
    private FileManager fileManager;
    private ChunkBackupSubprotocol chunkBackupSubprotocol;
    private ChunkRestoreSubprotocol chunkRestoreSubprotocol;
    private FileDeletionSubprotocol fileDeletionSubprotocol;
    private SpaceReclaimingSubprotocol spaceReclaimingSubprotocol;
    
    public BackupService(DistributedBackupService service) throws RemoteException {
        chunkBackupSubprotocol = service.getChunkBackupSubprotocol();
        chunkRestoreSubprotocol = service.getChunkRestoreSubprotocol();
        fileDeletionSubprotocol = service.getFileDeletionSubprotocol();
        spaceReclaimingSubprotocol = service.getSpaceReclaimingSubprotocol();
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
                FileUtils.setFileMetadata(filepath, metadata);
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

    public void reclaimSpace(int amount) {
        new Thread(() -> spaceReclaimingSubprotocol.reclaim(amount)).start();
    }

    public String status() {
        String ret = "";
        HashMap<String, FileData> backedUpFiles = fileManager.getBackedUpFiles();
	ret += "\n--------- Backed up files ---------\n\n";
        for (String fileId : backedUpFiles.keySet()) {
            FileMetadata metadata = fileManager.getFileMetadataByFileId(fileId);
            ret += "Pathname = '" + metadata.getFilepath() + "'\n";
            ret += "\tBackup Service Id = " + serverId + "\n";
            ret += "\tDesired Replication Degree = " + fileManager.getChunkDesiredReplicationDegree(fileId, 0) + "\n";
            HashMap<Integer, ChunkData> chunks = fileManager.getFileChunks(fileId);
            for (int chunkNo : chunks.keySet()) {
                ret += "\tChunk <" + fileId + ", " + chunkNo + ">\n";
                ret += "\t\tPerceived Replication Degree = " + fileManager.getChunkReplicationDegree(fileId, chunkNo) + "\n";
            }
        }

        HashMap<String, FileData> storedChunks = fileManager.getStoredChunks();
        if (storedChunks.size() != 0) {
            ret += "\n---------- Stored chunks ----------\n\n";
        }
        for (String fileId : storedChunks.keySet()) {
            HashMap<Integer, ChunkData> chunks = fileManager.getFileChunks(fileId);
            for (int chunkNo : chunks.keySet()) {
                ret += "Chunk <" + fileId + ", " + chunkNo + ">\n";
                ret += "\tSize = " + fileManager.getChunkSize(fileId, chunkNo) / 1000.0f + " KByte\n";
                ret += "\tPerceived Replication Degree = " + fileManager.getChunkReplicationDegree(fileId, chunkNo) + "\n";
            }
        }

        ret += "\nStorage = (" + fileManager.getUsedSpace() / 1000.0f + "/" + fileManager.getStorageSpace() / 1000.0f + ") KByte\n";
        return ret;
    }
}
