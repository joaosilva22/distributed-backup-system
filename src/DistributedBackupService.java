import communications.Message;
import communications.MessageHeader;
import communications.MessageBody;
import communications.ChannelMonitorThread;
import protocols.RequestDispatcher;
import services.BackupService;
import files.FileManager;
import utils.FileUtils;

import java.net.UnknownHostException;
import java.io.IOException;
// TODO: Apagar StandardCharsets, provavelmente nao esta a ser usado
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;

public class DistributedBackupService {
    private static ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<>();
    
    public static void main(String[] args) {
        if (args.length != 7) {
            System.out.println("Usage: java DistributedBackupService <server_id> <control_addr> <control_port> <backup_addr> <backup_port> <restore_addr> <restore_port>");
            return;
        }

        // TODO: Handling dos erros de parsing
        int serverId = Integer.parseInt(args[0]);
        String controlAddr = args[1];
        String backupAddr = args[3];
        String restoreAddr = args[5];
        int controlPort = Integer.parseInt(args[2]);
        int backupPort = Integer.parseInt(args[4]);
        int restorePort = Integer.parseInt(args[6]);

        try {
            new ChannelMonitorThread(controlAddr, controlPort, queue).start();
            new ChannelMonitorThread(backupAddr, backupPort, queue).start();
            new ChannelMonitorThread(restoreAddr, restorePort, queue).start();
            new RequestDispatcher(serverId, queue, backupAddr, backupPort, controlAddr, controlPort).start();
        } catch (UnknownHostException e) {
            // TODO: Lidar com esta excecao
            //       Provavelmente guardar todos os dados e fechar
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: Com esta tambem
            e.printStackTrace();
        }

        FileManager manager = new FileManager();

        try {
            BackupService backup = new BackupService();
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Backup", backup);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            byte[] data = "TEST DATA PLEASE IGNORE".getBytes();
            FileUtils.createFile("./backups/test.txt", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
