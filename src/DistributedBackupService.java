import protocol.MessageHeader;
import protocol.MessageBody;

public class DistributedBackupService {
    public static void main(String[] args) {
        /*if (args.length != 7) {
            System.out.println("Usage: java DistributedBackupService <server_id> <control_addr> <control_port> <backup_addr> <backup_port> <restore_addr> <restore_port>");
            return;
            }*/

        MessageHeader header = new MessageHeader()
            .setMessageType("A")
            .setFileId("A")
            .setVersion(1.0f)
            .setSenderId(1)
            .setChunkNo(1)
            .setReplicationDeg(1);
        byte[] bytes = header.getBytes();
        for (byte c : bytes) {
            System.out.printf("0x%02X ", c);
        }
        System.out.println();

        MessageBody body = new MessageBody().setContent("A B C D");
        byte[] bodyBytes = body.getBytes();
        for (byte c: bodyBytes) {
            System.out.printf("0x%02X ", c);
        }
        System.out.println();
    }
}
