package Server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.*;

public class ServerMain {
    public static ServerDS serverds;
    public static ServerSocket socket;
    public static ConcurrentHashMap<String, Socket> logins = new ConcurrentHashMap<>();
    public static ConcurrentLinkedQueue<Socket> clientsockets = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        if(args.length != 1){
            System.err.println(Colors.RED + "Invalid Parameters." + Colors.RESET);
            System.out.println("Usage: java ServerMain <ConfigFile path>");
            return;
        }
        File configfile = new File(args[0]);
        SConfigs serverconfig;
        serverconfig = SConfigs.getConfigs(configfile);
        serverds = new ServerDS();
        serverds.loadBackup();

        ServerRMI serverRMI = new ServerRMI();
        try{
            ServerRMIInt rmiint = (ServerRMIInt) UnicastRemoteObject.exportObject(serverRMI, 7777);
            Registry reg = LocateRegistry.createRegistry(serverconfig.rmiPort);
            reg.rebind(serverconfig.rmiName, rmiint);
        } catch (RemoteException e) {
            System.exit(-1);
        }

        Thread backupperThread = new Thread(new Backupper(serverds, serverconfig.backupTimer));
        backupperThread.start();

        Thread rewarder = new Thread(new Rewarder(serverds, serverconfig));
        rewarder.start();

        ThreadPoolExecutor workers = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        try {
            socket = new ServerSocket(serverconfig.port);
            System.out.println("Server ready to handle requests");
        } catch (IOException e) {
            System.err.println("Socket error");
            return;
        }

        while(true){
            try {
                Socket clientsock = socket.accept();
                System.out.println(Colors.BLUE + "Connection estabilished with client: " + clientsock.getRemoteSocketAddress() + Colors.RESET);
                clientsockets.add(clientsock);
                workers.submit(new RequestHandler(serverds, clientsock, true, serverRMI));
            } catch (SocketException e) {
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        workers.shutdown();

        try{
            if(!workers.awaitTermination(2, TimeUnit.SECONDS)){
                workers.shutdownNow();
            }
        } catch (InterruptedException e) {
            workers.shutdownNow();
        }

        backupperThread.interrupt();
        rewarder.interrupt();

        try{
            UnicastRemoteObject.unexportObject(serverRMI, false);
        } catch (NoSuchObjectException ignored) {
        }
        System.exit(0);
    }
}
