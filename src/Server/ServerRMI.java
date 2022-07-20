package Server;

import Client.ClientRMIInt;
import Server.exception.InvalidUserException;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerRMI extends RemoteServer implements ServerRMIInt {

    private ConcurrentHashMap<String, ClientRMIInt> clientsint = new ConcurrentHashMap<>();

    @Override
    public boolean register(String username, String password, HashSet<String> tags) throws RemoteException {
        ServerDS serverds = ServerMain.serverds;
        if(serverds.contains(username)){
            return false;
        }
        else{
            serverds.addUser(new User(username, password, tags));
            return true;
        }
    }

    @Override
    public void registerCallback(String username, ClientRMIInt clientint) throws RemoteException {
        clientsint.putIfAbsent(username, clientint);
    }

    @Override
    public void unregisterCallback(String username) throws RemoteException {
        clientsint.remove(username);
    }

    @Override
    public void notify(String username, String tonotify, String action) throws RemoteException {
        if(clientsint.containsKey(username)){
            clientsint.get(username).notify(tonotify, action);
        }
    }

    @Override
    public ConcurrentLinkedQueue<String> getFollowers(String username) throws RemoteException {
        ConcurrentLinkedQueue<String> ret;
        try{
            ret = ServerMain.serverds.getFollowers(username);
            return ret;
        } catch (InvalidUserException e){
            System.err.println(Colors.RED + "Invalid User" + Colors.RESET);
        } catch (NullPointerException ignore){
        }
        return null;
    }


}
