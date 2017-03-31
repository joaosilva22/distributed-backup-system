package clients;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {
    void Success(String s) throws RemoteException;
    void Failure(String s) throws RemoteException;
}