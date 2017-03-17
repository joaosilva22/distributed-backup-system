import protocol.Message;
import protocol.MessageHeader;
import protocol.MessageBody;
import protocol.ChannelMonitorThread;
import protocol.RequestDispatcher;
import utils.FileUtils;

import java.net.UnknownHostException;
import java.io.IOException;
// TODO: Apagar StandardCharsets, provavelmente nao esta a ser usado
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

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
            new ChannelMonitorThread(restoreAddr, backupPort, queue).start();
            new RequestDispatcher(queue).start();
        } catch (UnknownHostException e) {
            // TODO: Lidar com esta excecao
            //       Provavelmente guardar todos os dados e fechar
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: Com esta tambem
            e.printStackTrace();
        }
    }
}
