import protocol.Message;
import protocol.MessageHeader;
import protocol.MessageBody;
import protocol.ControlMonitorThread;
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
            //return;
        }

        int serverId = Integer.parseInt(args[0]);
        
        String controlAddr = args[1];
        int controlPort = Integer.parseInt(args[2]);

        try {
            new ControlMonitorThread(controlAddr, controlPort, queue).start();
        } catch (UnknownHostException e) {
            // TODO: Lidar com esta excecao
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: Com esta tambem
            e.printStackTrace();
        }
        
        byte[] data = "PUTCHUNK 1.0 1 a 1 1\r\nSTORED 1.0 1 a 1\r\n\r\n ABCD".getBytes(StandardCharsets.UTF_8);
        Message message = new Message(data);
        for (byte b : message.getBytes()) {
            System.out.printf("0x%02X ", b);
        }
        System.out.println();
    }
}
