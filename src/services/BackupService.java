package services;

import main.DistributedBackupService;
import utils.FileUtils;
import protocols.ChunkBackupSubprotocol;
import files.FileManager;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class BackupService extends UnicastRemoteObject implements BackupServiceInterface {
    private int serverId;
    private FileManager fileManager;
    private ChunkBackupSubprotocol chunkBackupSubprotocol;
    
    public BackupService(DistributedBackupService service) throws RemoteException {
        chunkBackupSubprotocol = service.getChunkBackupSubprotocol();
        serverId = service.getServerId();
        fileManager = service.getFileManager();
    }

    public void backupFile(String filepath, int replicationDeg) {
        // TODO: Dividir o ficheiro em chunks e chamar o initPutchunk do
        //       chunkBackupSubprotocol para cada um deles.
        String fileId = FileUtils.getFileId(filepath);
        // TODO: Substituir 64000 por uma constante
        ArrayList<byte[]> chunks = FileUtils.getFileChunks(filepath, BackupServiceConstants.CHUNK_SIZE);

        for (int i = 0; i < chunks.size(); i++) {
            final int chunkNo = i;
            final byte[] chunk = chunks.get(i);
            // TODO: Provavelmente aqui a versao nao devia ser uma constante
            //       mas por outro lado nao sei o que devia ser
            new Thread(() -> chunkBackupSubprotocol.initPutchunk(1.0f, serverId, fileId, chunkNo, replicationDeg, chunk)).start();
        }
    }

    // TODO: Adicionar os metodos relativos aos restantes protocolos
    //       ...
}
