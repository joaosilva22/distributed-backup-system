package services;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class BackupService extends UnicastRemoteObject implements BackupServiceInterface {
    public BackupService() throws RemoteException {}

    public void backupFile() {
        System.out.println("MOVE BITCH GET OFF THE PITCH");
    }

    // TODO: Adicionar os metodos relativos aos restantes protocolos
    //       ...
}
