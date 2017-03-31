package clients;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    private Client() {}

    public static void main(String[] args ) {
        //TODO -> Verificações

        try{
            Registry registry = LocateRegistry.getRegistry(host);
        }catch(Exception e){
            System.err.println("Client exception: ");
            e.printStackTrace();
        }
    }
}

package clients;

        import services.BackupServiceInterface;

        import java.rmi.registry.Registry;
        import java.rmi.registry.LocateRegistry;

public class Client implements ClientInterface{
    private static final int RMI_PORT = 1099;

    public Client() {}

    public static void main(String[] args) {
        if(args.length < 2){
            System.out.println("Usage: java Client <peer_ap> <operation> <opnd_1> <opnd_2>");
            return;
        }
        String peer_ap = args[0]
        String protocol = args[1];
        String filepath;
        int replication;
        switch (protocol) {
            case "BACKUP":
                if (args.length != 4) {
                    System.out.println("Usage: java Client <peer_ap> BACKUP <file_path> <replication_degree>");
                    return;
                }
                filepath = args[2];
                replication = Integer.parseInt(args[3]);
                break;
            case "RESTORE":
                if (args.length != 3) {
                    System.out.println("Usage: java Client <peer_ap> RESTORE <file_path>");
                    return;
                }
                filepath = args[3];
                break;
            case "DELETE":
                if (args.length != 3) {
                    System.out.println("Usage: java Client <peer_ap> DELETE <file_path>");
                    return;
                }
                filepath = args[3];
                break;
            default:
                System.out.println("Usage: java Client <peer_ap> <operation> <opnd_1> <opnd_2>");
                return;
        }

        Registry registry = null;
        try {
            Client client = new Client();
            Callback stubClient = (Callback) UnicastRemoteObject.exportObject(client,0);
            try{
                registry = LocateRegistry.createRegistry(RMI_PORT);
            } catch (ExportException e) {
                registry = LocateRegistry.getRegistry();
            }
            try{
                registry.bind("client", stubClient);
            } catch (AlreadyBoundException e) {
                e.printStackTrace();
            }
            BackupServiceInterface backup = (BackupServiceInterface) registry.lookup("Backup");
            switch (protocol) {
                case "BACKUP":
                    backup.backupFile(filepath, 1);
                    break;
                case "RESTORE":
                    backup.restoreFile(filepath);
                    break;
                case "DELETE":
                    backup.deleteFile(filepath);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Success(String s){
        System.out.println(s);
    }

    public void Failure(String s){
        System.out.println(s);
    }
}