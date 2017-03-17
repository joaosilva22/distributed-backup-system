package protocol;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataBackupSubprotocolInterface extends Remote {
    public void backupFile() throws RemoteException;    
}
