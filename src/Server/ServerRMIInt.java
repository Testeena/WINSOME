package Server;

import Client.ClientRMIInt;
import Server.exception.InvalidUserException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface ServerRMIInt extends Remote {
    public boolean register(String username, String password, HashSet<String> tags) throws RemoteException;

    public void registerCallback(String username, ClientRMIInt clientint) throws RemoteException;

    public void unregisterCallback(String username) throws RemoteException;

    public void notify(String username, String tonotify, String action) throws RemoteException;

    public ConcurrentLinkedQueue<String> getFollowers(String username) throws RemoteException, InvalidUserException;
}
