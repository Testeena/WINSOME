package Client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientRMIInt extends Remote {
    public void notify(String username, String action) throws RemoteException;
}
