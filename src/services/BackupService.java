package services;

import main.DistributedBackupService;
import utils.FileUtils;
import protocols.ChunkBackupSubprotocol;
import protocols.ChunkRestoreSubprotocol;
import files.FileManager;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class BackupService extends UnicastRemoteObject implements BackupServiceInterface {
    private int serverId;
    private FileManager fileManager;
    private ChunkBackupSubprotocol chunkBackupSubprotocol;
    private ChunkRestoreSubprotocol chunkRestoreSubprotocol;
    
    public BackupService(DistributedBackupService service) throws RemoteException {
        chunkBackupSubprotocol = service.getChunkBackupSubprotocol();
        chunkRestoreSubprotocol = service.getChunkRestoreSubprotocol();
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

    public void restoreFile(String filepath) {
        // TODO: Get file id devia funcionar apenas com o nome do ficheiro
        //       e nao utilizar todo o caminho, e nao sei se ja funciona assim
        String fileId = FileUtils.getFileId(filepath);
        int chunkNo = 0;
        new Thread(() -> chunkRestoreSubprotocol.initGetchunk(1.0f, serverId, fileId, chunkNo)).start();
    }

    // TODO: Adicionar os metodos relativos aos restantes protocolos
    //       ...
}
