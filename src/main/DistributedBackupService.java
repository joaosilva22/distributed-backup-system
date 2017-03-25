package main;

import communications.Message;
import communications.MessageHeader;
import communications.MessageBody;
import communications.ChannelMonitorThread;
import protocols.RequestDispatcher;
import protocols.ChunkBackupSubprotocol;
import protocols.ChunkRestoreSubprotocol;
import services.BackupService;
import services.BackupServiceInterface;
import files.FileManager;
import files.FileManagerConstants;
import utils.IOUtils;

import java.net.UnknownHostException;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.AlreadyBoundException;
import java.rmi.server.ExportException;

public class DistributedBackupService {
    private static final int RMI_PORT = 1099;
    
    private int serverId;
    private String mcAddr, mdbAddr, mdrAddr;
    private int mcPort, mdbPort, mdrPort;
    
    private static ConcurrentLinkedQueue<Message> queue;
    private FileManager fileManager;
    private ChunkBackupSubprotocol chunkBackupSubprotocol;
    private ChunkRestoreSubprotocol chunkRestoreSubprotocol;

    public DistributedBackupService(int serverId, String mcAddr, int mcPort, String mdbAddr, int mdbPort, String mdrAddr, int mdrPort) throws IOException, ClassNotFoundException {
        this.serverId = serverId;
        this.mcAddr = mcAddr;
        this.mcPort = mcPort;
        this.mdbAddr = mdbAddr;
        this.mdbPort = mdbPort;
        this.mdrAddr = mdbAddr;
        this.mdrPort = mdrPort;
        
        queue = new ConcurrentLinkedQueue<>();
        File f = new File(FileManagerConstants.PATH + FileManagerConstants.METADATA_FILENAME + serverId);
        if (f.exists()) {
            FileInputStream fileIn = new FileInputStream(f);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            fileManager = (FileManager) in.readObject();
            in.close();
            fileIn.close();
        } else {
            fileManager = new FileManager();
        }
        chunkBackupSubprotocol = new ChunkBackupSubprotocol(this);
        chunkRestoreSubprotocol = new ChunkRestoreSubprotocol(this);
    }

    public void init() {
        try {
            new ChannelMonitorThread(mcAddr, mcPort, queue).start();
            new ChannelMonitorThread(mdbAddr, mdbPort, queue).start();
            new ChannelMonitorThread(mdrAddr, mdrPort, queue).start();
            new RequestDispatcher(this).start();
        } catch (UnknownHostException e) {
            IOUtils.err("DistributedBackupService error: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            IOUtils.err("DistributedBackupService error: " + e.toString());
            e.printStackTrace();
        }

        Registry registry;
        try {
            BackupService backup = new BackupService(this);
            try {
                registry = LocateRegistry.createRegistry(RMI_PORT);
                registry.bind("Backup", backup);
            } catch (ExportException e) {
                IOUtils.warn("DistributedBackupService warning: " + e.toString());
                registry = LocateRegistry.getRegistry(RMI_PORT);
            } catch (AlreadyBoundException e) {
                IOUtils.warn("DistributedBackupService warning: " + e.toString());
            }
        } catch (RemoteException e) {
            IOUtils.err("DistributedBackupService error: " + e.toString());
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

    public ChunkRestoreSubprotocol getChunkRestoreSubprotocol() {
        return chunkRestoreSubprotocol;
    }
        
    public static void main(String[] args) {
        if (args.length != 7) {
            System.out.println("Usage: java DistributedBackupService <server_id> <control_addr> <control_port> <backup_addr> <backup_port> <restore_addr> <restore_port>");
            return;
        }

        // TODO: Handling dos erros de parsing
        DistributedBackupService service = null;
        try {
            service = new DistributedBackupService(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]), args[5], Integer.parseInt(args[6]));
            service.init();
        } catch (IOException e) {
            IOUtils.err("DistributedBackupService error: " + e.toString());
            e.printStackTrace();
            return;
        } catch (ClassNotFoundException e) {
            IOUtils.err("DistributedBackupService error: " + e.toString());
            e.printStackTrace();
            return;
        }

        IOUtils.log("Starting server (id=" + service.serverId + ") with params:");
        IOUtils.log("Multicast Control Channel: <" + service.mcAddr + ", " + service.mcPort + ">");
        IOUtils.log("Multicast Data Backup Channel: <" + service.mdbAddr + ", " + service.mdbPort + ">");
        IOUtils.log("Multicast Data Recovery Channel: <" + service.mdrAddr + ", " + service.mdrPort + ">");
    }
}
