package Client;

import Server.Colors;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

public class ClientRMI extends RemoteObject implements ClientRMIInt{

    @Override
    public void notify(String username, String action) throws RemoteException {
        if(action.equals("follow")){
            ClientMain.followers.add(username);
            System.out.println(Colors.YELLOW + username + " started following you." + Colors.RESET);
        }
        else if (action.equals("unfollow")){
            ClientMain.followers.remove(username);
            System.out.println(Colors.YELLOW + username + " unfollowed you." + Colors.RESET);
        }
    }
}
