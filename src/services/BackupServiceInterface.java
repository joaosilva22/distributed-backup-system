package services;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BackupServiceInterface extends Remote {
    public void backupFile(String filepath, int replicationDeg) throws RemoteException;    
}