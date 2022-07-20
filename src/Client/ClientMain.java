package Client;

import Server.Colors;
import Server.ServerRMIInt;
import Server.exception.InvalidUserException;

import java.io.*;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientMain {
    private static String user;
    public static ConcurrentLinkedQueue<String> followers = new ConcurrentLinkedQueue<>();
    public static void main(String[] args) {
        if(args.length != 1){
            System.err.println(Colors.RED + "< Invalid Parameters." + Colors.RESET);
            System.out.println("< Usage: java ClientMain <ConfigFile path>");
            help();
            return;
        }
        File configfile = new File(args[0]);
        CConfigs clientconfig;
        clientconfig = CConfigs.getConfigs(configfile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean isconnected = false;
        boolean cantregister = false;

        Socket socket = null;
        try{
            socket = new Socket(clientconfig.address, clientconfig.port);
            isconnected = true;
            System.out.println("< Connection estabilished.");
        } catch (IOException e) {
            System.err.println(Colors.RED + "< Socket connect error" + Colors.RESET);
            try {
                socket.close();
            } catch (IOException | NullPointerException ignore) {
            }
            System.out.println("Client terminated.");
            return;
        }

        WalletUpdater walletupdater = new WalletUpdater(clientconfig.mcastAddress, clientconfig.port);
        Thread walletupdaterthread = new Thread(walletupdater);
        walletupdaterthread.start();

        ServerRMIInt serverRMI = null;
        ClientRMIInt clientRMIobj = null;
        ClientRMIInt objstub;

        while(isconnected){
            try {
                System.out.print(Colors.RESET + "> ");
                String command = reader.readLine();
                if(command.isEmpty()){
                    continue;
                }
                if(command.contains("help")){
                    help();
                    continue;
                }
                String[] tokens = command.split(" ");
                if(command.contains("register")){
                    if(cantregister){
                        continue;
                    }
                    if(tokens.length < 4 || tokens.length > 8){
                        System.out.println(Colors.RED + "< Wrong arguments, you may need to give us some tags!" + Colors.RESET);
                        continue;
                    }
                    user = tokens[1];
                    HashSet<String> tags = new HashSet<>(Arrays.asList(tokens).subList(3, tokens.length));
                    Registry registry;
                    ServerRMIInt registration;
                    try{
                        registry = LocateRegistry.getRegistry(clientconfig.address, clientconfig.rmiPort);
                        registration = (ServerRMIInt) registry.lookup(clientconfig.rmiName);
                    } catch (NotBoundException e) {
                        e.printStackTrace();
                        return;
                    }

                    if(registration.register(tokens[1], tokens[2], tags)){
                        System.out.println("< " + Colors.GREEN + "Registration completed!" + Colors.RESET);
                    } else{
                        System.out.println(Colors.RED + "< Username already used." + Colors.RESET);
                    }
                    continue;
                }

                if(command.contains("list followers")){
                    if(cantregister){
                        System.out.print("< Followers: ");
                        if(followers == null){
                            System.out.println("EMPTY");
                        }
                        else {
                            System.out.println(followers);
                        }
                    }
                    else{
                        System.out.println("< " + Colors.RED + "Login needed" + Colors.RESET);
                    }
                    continue;
                }

                if(command.contains("exit")){
                    isconnected = false;
                }

                try {
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    int lenghtbytes = command.getBytes().length;
                    writer.println(lenghtbytes);
                    writer.print(command);
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String response = "";
                try {
                    BufferedReader sockreader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String lenghtstring = sockreader.readLine();
                    if(lenghtstring == null){
                        response = "Unknown";
                    }
                    else {
                        StringBuilder s = new StringBuilder();
                        int lenght = Integer.parseInt(lenghtstring);
                        for (int i = 0; i < lenght; i++) {
                            s.append((char) sockreader.read());
                        }
                        response = s.toString();
                    }
                    System.out.println("< Response: " + response);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(command.contains("login") && response.contains("OK")){
                    user = tokens[1];
                    cantregister = true;
                    try{
                        Registry registry = LocateRegistry.getRegistry(clientconfig.rmiPort);
                        serverRMI = (ServerRMIInt) registry.lookup(clientconfig.rmiName);
                        clientRMIobj = new ClientRMI();
                        objstub = (ClientRMIInt) UnicastRemoteObject.exportObject(clientRMIobj, 0);
                        serverRMI.registerCallback(user, objstub);
                        followers = serverRMI.getFollowers(user);
                    } catch (NotBoundException e) {
                        System.err.println(Colors.RED + "RMI followers update failed." + Colors.RESET);
                        walletupdater.stopWorking();
                        socket.close();
                        return;
                    } catch (InvalidUserException e) {
                        System.err.println(Colors.RED + "Invalid User" + Colors.RESET);
                    }
                }

                if(command.contains("logout") && response.contains("OK")){
                    try{
                        assert serverRMI != null;
                        serverRMI.unregisterCallback(user);
                    } catch (RemoteException e) {
                        System.err.println(Colors.RED + "RMI unregister failed." + Colors.RESET);
                    }
                    user = null;
                    followers = new ConcurrentLinkedQueue<>();
                    cantregister = false;
                }
            } catch (IOException e) {
                System.err.println(Colors.RED + "< Command execution failed." + Colors.RESET);
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
                break;
            }
        }

        walletupdater.stopWorking();
        try{
            if(user != null && serverRMI != null){
                serverRMI.unregisterCallback(user);
            }
            if(clientRMIobj != null){
                UnicastRemoteObject.unexportObject(clientRMIobj, false);
            }
        } catch (RemoteException e) {
            System.err.println(Colors.RED + "RMI unregister failed." + Colors.RESET);
        }
        try{
            socket.close();
            reader.close();
        } catch (IOException ignored) {
        }
        System.out.println(Colors.BLUE + "Shutting down Client." + Colors.RESET);
        System.exit(0);
    }

    public static void help(){
        System.out.println("""
            List of possible commands:
            register <username> <password> <tags>      register a new User
            login <username> <password>                login as a previously registered User
            logout                                     logout from the service
            exit                                       terminate Client Process
            list users                                 prints the list of users with one (or more) tags in common with logged user
            list followers                             prints the list of logged user's followers
            list following                             prints the list of users followed by logged user
            follow <username>                          follow passed user
            unfollow <username>                        unfollow passed user
            blog                                       prints the list of logged user's posts
            post <title>;<content>                     creates a new post with given title and content
            show feed                                  prints logged user's feed posts
            show post <id>                             prints passed posts info
            delete <postid>                            deletes passed post
            rewin <postid>                             rewins passed post
            rate <postid> <rate>                       adds rate to passed post
            comment <postid> <content>                 adds comment to passed post
            wallet                                     prints logged user's wallet current balance
            wallet btc                                 prints logged user's wallet current balance in BitCoin""");
    }
}
