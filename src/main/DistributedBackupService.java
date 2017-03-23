package main;

import communications.Message;
import communications.MessageHeader;
import communications.MessageBody;
import communications.ChannelMonitorThread;
import protocols.RequestDispatcher;
import protocols.ChunkBackupSubprotocol;
import services.BackupService;
import services.BackupServiceInterface;
import files.FileManager;
import utils.IOUtils;

import java.net.UnknownHostException;
import java.io.IOException;
// TODO: Apagar StandardCharsets, provavelmente nao esta a ser usado
import java.util.concurrent.ConcurrentLinkedQueue;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;

public class DistributedBackupService {
    private int serverId;
    private String mcAddr, mdbAddr, mdrAddr;
    private int mcPort, mdbPort, mdrPort;
    
    private static ConcurrentLinkedQueue<Message> queue;
    private FileManager fileManager;
    private ChunkBackupSubprotocol chunkBackupSubprotocol;

    public DistributedBackupService(int serverId, String mcAddr, int mcPort, String mdbAddr, int mdbPort, String mdrAddr, int mdrPort) {
        this.serverId = serverId;
        this.mcAddr = mcAddr;
        this.mcPort = mcPort;
        this.mdbAddr = mdbAddr;
        this.mdbPort = mdbPort;
        this.mdrAddr = mdbAddr;
        this.mdrPort = mdrPort;
        
        queue = new ConcurrentLinkedQueue<>();
        fileManager = new FileManager();
        chunkBackupSubprotocol = new ChunkBackupSubprotocol(this);
    }

    public void init() {
        try {
            new ChannelMonitorThread(mcAddr, mcPort, queue).start();
            new ChannelMonitorThread(mdbAddr, mdbPort, queue).start();
            new ChannelMonitorThread(mdrAddr, mdrPort, queue).start();
            new RequestDispatcher(this).start();
        } catch (UnknownHostException e) {
            IOUtils.log("DistributedBackupService error: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            IOUtils.log("DistributedBackupService error: " + e.toString());
            e.printStackTrace();
        }

        try {
            BackupService backup = new BackupService(this);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Backup", backup);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getServerId() {
        return serverId;
    }

    public String getMcAddr() {
        return mcAddr;
    }

    public int getMcPort() {
        return mcPort;
    }

    public String getMdbAddr() {
        return mdbAddr;
    }

    public int getMdbPort() {
        return mdbPort;
    }

    public String getMdrAddr() {
        return mdrAddr;
    }

    public int getMdrPort() {
        return mdrPort;
    }

    public ConcurrentLinkedQueue<Message> getQueue() {
        return queue;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public ChunkBackupSubprotocol getChunkBackupSubprotocol() {
        return chunkBackupSubprotocol;
    }
        
    public static void main(String[] args) {
        if (args.length != 7) {
            System.out.println("Usage: java DistributedBackupService <server_id> <control_addr> <control_port> <backup_addr> <backup_port> <restore_addr> <restore_port>");
            return;
        }

        IOUtils.log("Began service...");

        // TODO: Handling dos erros de parsing
        DistributedBackupService service = new DistributedBackupService(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]), args[5], Integer.parseInt(args[6]));
        service.init();
    }
}