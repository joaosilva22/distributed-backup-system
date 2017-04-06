package tests;

import services.BackupServiceInterface;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class RMITest {
    public RMITest() {}

    public static void main(String[] args) {
        String protocol = args[0];
        
        try {
            Registry registry = LocateRegistry.getRegistry();
            BackupServiceInterface backup = (BackupServiceInterface) registry.lookup("Backup");
            switch (protocol) {
                case "PUTCHUNK":
                    backup.backupFile(args[1], 1);
                    break;
                case "GETCHUNK":
                    backup.restoreFile(args[1]);
                    break;
                case "DELETE":
                    backup.deleteFile(args[1]);
                    break;
                case "RECLAIM":
                    backup.reclaimSpace(Integer.parseInt(args[1]));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
