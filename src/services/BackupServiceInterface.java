package services;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BackupServiceInterface extends Remote {
    public void backupFile(String filepath, int replicationDeg) throws RemoteException;

    public void restoreFile(String filepath) throws RemoteException;

    public void deleteFile(String filepath) throws RemoteException;

    public void reclaimSpace(int amount) throws RemoteException;
}
