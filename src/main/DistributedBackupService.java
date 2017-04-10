package main;

import communications.Message;
import communications.MessageHeader;
import communications.MessageBody;
import communications.ChannelMonitorThread;
import communications.UnicastMonitorThread;
import protocols.RequestDispatcher;
import protocols.ChunkBackupSubprotocol;
import protocols.ChunkRestoreSubprotocol;
import protocols.FileDeletionSubprotocol;
import protocols.SpaceReclaimingSubprotocol;
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
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.AlreadyBoundException;
import java.rmi.server.ExportException;

public class DistributedBackupService {
    private static final int RMI_PORT = 1099;

    private float version;
    private int serverId;
    private String accessPoint;
    private String mcAddr, mdbAddr, mdrAddr;
    private int mcPort, mdbPort, mdrPort;
    private int unicastPort;
    
    private static ConcurrentLinkedQueue<Message> queue;
    private FileManager fileManager;
    private ChunkBackupSubprotocol chunkBackupSubprotocol;
    private ChunkRestoreSubprotocol chunkRestoreSubprotocol;
    private FileDeletionSubprotocol fileDeletionSubprotocol;
    private SpaceReclaimingSubprotocol spaceReclaimingSubprotocol;

    public DistributedBackupService(float version, int serverId, String accessPoint, String mcAddr, int mcPort, String mdbAddr, int mdbPort, String mdrAddr, int mdrPort) throws IOException, ClassNotFoundException {
        this.version = version;
        this.serverId = serverId;
        this.accessPoint = accessPoint;
        this.mcAddr = mcAddr;
        this.mcPort = mcPort;
        this.mdbAddr = mdbAddr;
        this.mdbPort = mdbPort;
        this.mdrAddr = mdrAddr;
        this.mdrPort = mdrPort;
        
        queue = new ConcurrentLinkedQueue<>();
        File f = new File(FileManagerConstants.PATH + FileManagerConstants.METADATA_FILENAME + serverId);
        if (f.exists()) {
            FileInputStream fileIn = new FileInputStream(f);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            fileManager = (FileManager) in.readObject();
            fileManager.init();
            in.close();
            fileIn.close();
        } else {
            fileManager = new FileManager();
        }
        chunkBackupSubprotocol = new ChunkBackupSubprotocol(this);
        chunkRestoreSubprotocol = new ChunkRestoreSubprotocol(this);
        fileDeletionSubprotocol = new FileDeletionSubprotocol(this);
        spaceReclaimingSubprotocol = new SpaceReclaimingSubprotocol(this);

        unicastPort = 8000;
        while (unicastPort == mcPort || unicastPort == mdbPort || unicastPort == mdrPort) {
            unicastPort += 1;
        }
    }

    public void init() {
        try {
            new ChannelMonitorThread(mcAddr, mcPort, queue).start();
            new ChannelMonitorThread(mdbAddr, mdbPort, queue).start();
            new ChannelMonitorThread(mdrAddr, mdrPort, queue).start();
            if (version == 1.1f) {
                new UnicastMonitorThread(unicastPort, queue).start();
            }
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
                registry.bind(accessPoint, backup);
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

    public float getVersion() {
        return version;
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

    public int getUnicastPort() {
        return unicastPort;
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

    public FileDeletionSubprotocol getFileDeletionSubprotocol() {
        return fileDeletionSubprotocol;
    }

    public SpaceReclaimingSubprotocol getSpaceReclaimingSubprotocol() {
        return spaceReclaimingSubprotocol;
    }
        
    public static void main(String[] args) {
        if (args.length != 9) {
            System.out.println("Usage: java DistributedBackupService <version> <server_id> <access_point> <control_addr> <control_port> <backup_addr> <backup_port> <restore_addr> <restore_port>");
            return;
        }

        // TODO: Handling dos erros de parsing
        DistributedBackupService service = null;
        try {
            service = new DistributedBackupService(Float.parseFloat(args[0]), Integer.parseInt(args[1]), args[2], args[3], Integer.parseInt(args[4]), args[5], Integer.parseInt(args[6]), args[7], Integer.parseInt(args[8]));
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

        if (Float.parseFloat(args[0]) == 1.1f) {
            try {
                ArrayList<byte[]> initPutchunkInfo = service.fileManager.getInitPutchunkInfo();
                for (byte[] data : initPutchunkInfo) {
                    String content = new String(data);
                    String[] parts = content.split(" ");
                    float version = Float.parseFloat(parts[0]);
                    int senderId = Integer.parseInt(parts[1]);
                    int replicationDegree = Integer.parseInt(parts[2]);
                    int chunkNo = Integer.parseInt(parts[3]);
                    String fileId = parts[4];
                    String chunkContent = parts[5];
                    for (int i = 6; i < parts.length; i++) {
                        chunkContent += " " + parts[i];
                    }
                    byte[] chunk = chunkContent.getBytes();
                    final DistributedBackupService fService = service;
                    new Thread(() -> fService.chunkBackupSubprotocol.enhancedInitPutchunk(version, senderId, fileId, chunkNo, replicationDegree, chunk)).start();
                }
            } catch (IOException e) {
                IOUtils.err("DistributedBackupService error: " + e.toString());
                e.printStackTrace();
            }
        }
    }
}
