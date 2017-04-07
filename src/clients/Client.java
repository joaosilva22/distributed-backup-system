package clients;

import services.BackupServiceInterface;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.AlreadyBoundException;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.io.File;

public class Client implements ClientInterface {
    private static final int RMI_PORT = 1099;

    public Client() {}

    public static void main(String[] args) {
        if(args.length < 2){
            System.out.println("Usage: java Client <peer_ap> <operation> <opnd_1> <opnd_2>");
            return;
        }
        String peer_ap = args[0];
        String protocol = args[1];
        String filepath = null;
        int amount = -1;
        int replication = -1;
        switch (protocol) {
            case "BACKUP":
                if (args.length != 4) {
                    System.out.println("Usage: java Client <peer_ap> BACKUP <file_path> <replication_degree>");
                    return;
                }
                filepath = args[2];
		File file = new File(filepath);
		if(!file.exists() || file.isDirectory()) {
		    System.out.println("Error: invalid file " + filepath);
		    return;
		}
                replication = Integer.parseInt(args[3]);
                break;
            case "RESTORE":
                if (args.length != 3) {
                    System.out.println("Usage: java Client <peer_ap> RESTORE <file_path>");
                    return;
                }
                filepath = args[2];
                break;
            case "DELETE":
                if (args.length != 3) {
                    System.out.println("Usage: java Client <peer_ap> DELETE <file_path>");
                    return;
                }
                filepath = args[2];
                break;
            case "RECLAIM":
                if(args.length != 3) {
                    System.out.println("Usage: java Client <peer_ap> RECLAIM <amount>");
                    return;
                }
                amount = 1000 * Integer.parseInt(args[2]);
                break;
	    case "STATUS":
                if(args.length != 2) {
                    System.out.println("Usage: java Client <peer_ap> STATUS");
                    return;
                }
                break;
            default:
                System.out.println("Usage: java Client <peer_ap> <operation> <opnd_1> <opnd_2>");
                return;
        }

        Registry registry = null;
        Client client = new Client();
        try {
            ClientInterface stubClient = (ClientInterface) UnicastRemoteObject.exportObject(client,0);
            try {
                registry = LocateRegistry.createRegistry(RMI_PORT);
            } catch (ExportException e) {
                registry = LocateRegistry.getRegistry();
            }
            try {
                registry.bind("client", stubClient);
            } catch (AlreadyBoundException e) {
                e.printStackTrace();
            }
            BackupServiceInterface backup = null;
	    try {
		backup = (BackupServiceInterface) registry.lookup(peer_ap);
	    } catch (Exception e) {
	        e.printStackTrace();  
            }
            switch (protocol) {
                case "BACKUP":
                    backup.backupFile(filepath, replication);
                    break;
                case "RESTORE":
                    backup.restoreFile(filepath);
                    break;
                case "DELETE":
                    backup.deleteFile(filepath);
                    break;
                case "RECLAIM":
                    backup.reclaimSpace(amount);
                    break;
		case "STATUS":
		    System.out.println(backup.status());
		    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            registry.unbind("client");
            UnicastRemoteObject.unexportObject(client,false);
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
