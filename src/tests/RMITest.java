package tests;

import services.BackupServiceInterface;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class RMITest {
    public RMITest() {}

    public static void main(String[] args) {
        String protocol = args[0];
        String filepath = args[1];
        
        try {
            Registry registry = LocateRegistry.getRegistry();
            BackupServiceInterface backup = (BackupServiceInterface) registry.lookup("Backup");
            switch (protocol) {
                case "PUTCHUNK":
                    backup.backupFile(filepath, 1);
                    break;
                case "GETCHUNK":
                    backup.restoreFile(filepath);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
