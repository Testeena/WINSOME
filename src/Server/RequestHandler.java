package Server;

import Server.exception.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestHandler implements Runnable{
    private final Boolean verbose;
    private final ServerDS serverds;
    private String user;
    private Socket clientsock;
    private boolean alreadylogged;
    ServerRMI serverrmi;

    public RequestHandler(ServerDS serverds, Socket clientsock, Boolean verbose, ServerRMI serverrmi){
        this.serverds = serverds;
        this.clientsock = clientsock;
        this.verbose = verbose;
        this.alreadylogged = false;
        this.serverrmi = serverrmi;
    }

    @Override
    public void run() {
        while (true) {
            String request = "";
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientsock.getInputStream()));
                StringBuilder s = new StringBuilder();
                String lenghtstring = reader.readLine();
                int lenght = Integer.parseInt(lenghtstring);
                for (int i = 0; i < lenght; i++) {
                    s.append((char) reader.read());
                }
                request = s.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String response = "";
            if (request.length() == 0) {
                if (verbose) System.out.println(Colors.RED + "RequestHandler: empty request" + Colors.RESET);
                response = "EMPTYREQ";
            } else {
                String[] tokens = request.split(" ");
                switch (tokens[0]) {
                    case "login" -> {
                        if (tokens.length != 3) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                            response = "BADREQ";
                            break;
                        }
                        if (!serverds.contains(tokens[1])) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: User Not Existing" + Colors.RESET);
                            response = "NOTEXISTS";
                            break;
                        }
                        if (alreadylogged) {
                            if (verbose) System.out.println(Colors.YELLOW + "RequestHandler: Already Logged User" + Colors.RESET);
                            response = "ALREADYLOGGED";
                            break;
                        }
                        if (serverds.getUser(tokens[1]).getPassword().equals(tokens[2])) {
                            ServerMain.logins.put(tokens[1], clientsock);
                            user = tokens[1];
                            setLoginBool(true);
                            response = "OK";
                        } else {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: Wrong Password" + Colors.RESET);
                            response = "DENIED";
                        }
                    }
                    case "logout" -> {
                        if (tokens.length != 1) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                            response = "BADREQ";
                            break;
                        }
                        if (!alreadylogged) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: User Not Logged" + Colors.RESET);
                            response = "NOTLOGGED";
                        } else {
                            ServerMain.logins.remove(user);
                            response = "OK";
                            setLoginBool(false);
                        }
                    }
                    case "list" -> {
                        if (tokens.length != 2) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                            response = "BADREQ";
                            break;
                        }
                        if (!alreadylogged) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: User Not Logged" + Colors.RESET);
                            response = "NOTLOGGED";
                            break;
                        }
                        switch (tokens[1]) {
                            case "users": {
                                try {
                                    HashSet<String> users = serverds.getSameTags(serverds.getUser(user));
                                    response = makeList(users);
                                    break;
                                } catch (InvalidUserException e) {
                                    if (verbose) System.out.println(Colors.RED + "RequestHandler: Invalid User" + Colors.RESET);
                                    response = "INVALIDUSER";
                                    break;
                                }
                            }
                            case "following": {
                                try {
                                    ConcurrentLinkedQueue<String> users = serverds.getFollowing(user);
                                    if(users == null){
                                        response = "EMPTY";
                                    }else{
                                        response = makeList(new HashSet<>(users));
                                    }
                                    break;
                                } catch (InvalidUserException e) {
                                    if (verbose) System.out.println(Colors.RED + "RequestHandler: Invalid User" + Colors.RESET);
                                    response = "INVALIDUSER";
                                    break;
                                }
                            }
                        }
                    }
                    case "follow" -> {
                        if (tokens.length != 2) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                            response = "BADREQ";
                        }
                        if (!alreadylogged) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: User Not Logged" + Colors.RESET);
                            response = "NOTLOGGED";
                        } else {
                            try {
                                serverds.follow(user, tokens[1]);
                                serverrmi.notify(tokens[1], user, "follow");
                                response = "OK";
                            } catch (InvalidUserException | RemoteException e) {
                                if (verbose) System.out.println(Colors.RED + "RequestHandler: Invalid User" + Colors.RESET);
                                response = "INVALIDUSER";
                            }
                        }
                    }
                    case "unfollow" -> {
                        if (tokens.length != 2) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                            response = "BADREQ";
                        }
                        if (!alreadylogged) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: User Not Logged" + Colors.RESET);
                            response = "NOTLOGGED";
                        } else {
                            try {
                                serverds.unfollow(user, tokens[1]);
                                serverrmi.notify(tokens[1], user, "unfollow");
                                response = "OK";
                            } catch (InvalidUserException | RemoteException e) {
                                if (verbose)
                                    System.out.println(Colors.RED + "RequestHandler: Invalid User" + Colors.RESET);
                                response = "INVALIDUSER";
                            }
                        }
                    }
                    case "blog" -> {
                        if (tokens.length != 1) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                            response = "BADREQ";
                            break;
                        }
                        if (!alreadylogged) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: User Not Logged" + Colors.RESET);
                            response = "NOTLOGGED";
                        } else {
                            try {
                                HashSet<Post> userspost = serverds.getUserBlog(user);
                                if (userspost.isEmpty()) {
                                    response = "EMPTY";
                                } else {
                                    StringBuilder s = new StringBuilder();
                                    for (Post p : userspost) {
                                        makeResponse(s, p);
                                    }
                                    response = "OK\n" + s;
                                }
                            } catch (InvalidUserException e) {
                                if (verbose)
                                    System.out.println(Colors.RED + "RequestHandler: Invalid User" + Colors.RESET);
                                response = "INVALIDUSER";
                            }
                        }
                    }
                    case "post" -> {
                        if (tokens.length < 2) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                            response = "BADREQ";
                            break;
                        }
                        if (!alreadylogged) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: User Not Logged" + Colors.RESET);
                            response = "NOTLOGGED";
                        } else {
                            if (!Arrays.toString(tokens).contains(";") || Arrays.toString(tokens).chars().filter(semicolon -> semicolon == ';').count() != 1) {
                                if (verbose)
                                    System.out.println(Colors.RED + "RequestHandler: Bad Request, semicolon (;) needed" + Colors.RESET);
                                response = "BADREQ";
                            } else {
                                try {
                                    String title = request.substring(5, request.indexOf(";"));
                                    String content = request.substring(request.indexOf(";") + 1);
                                    serverds.createPost(user, title, content);
                                    response = "OK";
                                } catch (InvalidUserException e) {
                                    if (verbose)
                                        System.out.println(Colors.RED + "RequestHandler: Invalid User" + Colors.RESET);
                                    response = "INVALIDUSER";
                                }
                            }
                        }
                    }
                    case "show" -> {
                        if (tokens.length > 3) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                            response = "BADREQ";
                            break;
                        }
                        if (!alreadylogged) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: User Not Logged" + Colors.RESET);
                            response = "NOTLOGGED";
                        } else {
                            switch (tokens[1]) {
                                case "feed" -> {
                                    try {
                                        HashSet<Post> userfeed = serverds.getUserFeed(user);
                                        if (userfeed.isEmpty()) {
                                            response = "EMPTY";
                                        } else {
                                            StringBuilder s = new StringBuilder();
                                            s.append("OK\n");
                                            for (Post p : userfeed) {
                                                makeResponse(s, p);
                                            }
                                            response = s.toString();
                                        }
                                    } catch (InvalidUserException e) {
                                        if (verbose)
                                            System.out.println(Colors.RED + "RequestHandler: Invalid User" + Colors.RESET);
                                        response = "INVALIDUSER";
                                    }
                                }
                                case "post" -> {
                                    if (tokens.length != 3) {
                                        if (verbose)
                                            System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                                        response = "BADREQ";
                                        break;
                                    }
                                    try {
                                        Post toshow = serverds.getPost(Integer.parseInt(tokens[2]));
                                        StringBuilder s = new StringBuilder();
                                        s.append("OK\n");
                                        makeResponse(s, toshow);
                                        s.append("Comments: ");
                                        if (toshow.getComments().isEmpty()) {
                                            s.append("EMPTY");
                                        } else {
                                            s.append("\n");
                                            for (Comment comm : toshow.getComments()) {
                                                s.append("\tAuthor: ").append(comm.getUsername()).append("\n");
                                                s.append("\tContent: ").append(comm.getContent()).append("\n");
                                            }
                                        }
                                        response = s.toString();
                                    } catch (InvalidPostIdException | NumberFormatException e) {
                                        if (verbose) System.out.println(Colors.RED + "RequestHandler: Invalid Postid" + Colors.RESET);
                                        response = "INVALIDPOSTID";
                                    }
                                }
                            }
                        }
                    }
                    case "delete" -> {
                        if (tokens.length != 2) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                            response = "BADREQ";
                            break;
                        }
                        if (!alreadylogged) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: User Not Logged" + Colors.RESET);
                            response = "NOTLOGGED";
                            break;
                        }
                        try {
                            serverds.deletePost(user, Integer.parseInt(tokens[1]));
                            response = "OK";
                        } catch (InvalidPostIdException | NumberFormatException e) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: Invalid PostID" + Colors.RESET);
                            response = "INVALIDPOSTID";
                        } catch (InvalidUserException e) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Invalid User" + Colors.RESET);
                            response = "INVALIDUSER";
                        } catch (NotYourPostException e){
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Permission Denied" + Colors.RESET);
                            response = "DENIED";
                        }
                    }
                    case "rewin" -> {
                        if (tokens.length != 2) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                            response = "BADREQ";
                            break;
                        }
                        if (!alreadylogged) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: User Not Logged" + Colors.RESET);
                            response = "NOTLOGGED";
                            break;
                        }
                        try {
                            serverds.rewinPost(user, Integer.parseInt(tokens[1]));
                            response = "OK";
                        } catch (InvalidPostIdException | NumberFormatException e) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: Invalid PostID" + Colors.RESET);
                            response = "INVALIDPOSTID";
                        } catch (InvalidUserException e) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Invalid User" + Colors.RESET);
                            response = "INVALIDUSER";
                        }
                    }
                    case "rate" -> {
                        if (tokens.length != 3) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                            response = "BADREQ";
                            break;
                        }
                        if (!alreadylogged) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: User Not Logged" + Colors.RESET);
                            response = "NOTLOGGED";
                            break;
                        }
                        try {
                            HashSet<Post> userfeed = serverds.getUserFeed(user);
                            Post torate = serverds.getPost(Integer.parseInt(tokens[1]));
                            if (userfeed.contains(torate)) {
                                serverds.votePost(user, Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
                                response = "OK";
                            } else {
                                if (verbose)
                                    System.out.println(Colors.RED + "RequestHandler: Post not in User's Feed" + Colors.RESET);
                                response = "DENIED";
                            }
                        } catch (NumberFormatException e) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: Invalid Parameter Types" + Colors.RESET);
                            response = "INVALIDPARAMS";
                        } catch (InvalidUserException e) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Invalid User" + Colors.RESET);
                            response = "INVALIDUSER";
                        } catch (InvalidPostIdException e) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: Invalid PostID" + Colors.RESET);
                            response = "INVALIDPOSTID";
                        } catch (InvalidVoteException e) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Invalid Vote" + Colors.RESET);
                            response = "INVALIDVOTE";
                        } catch (AlreadyVotedException e) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: Already Voted" + Colors.RESET);
                            response = "ALREADYVOTED";
                        }
                    }
                    case "comment" -> {
                        if (tokens.length < 3) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                            response = "BADREQ";
                            break;
                        }
                        if (!alreadylogged) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: User Not Logged" + Colors.RESET);
                            response = "NOTLOGGED";
                            break;
                        }
                        try {
                            HashSet<Post> userfeed = serverds.getUserFeed(user);
                            Post tocomment = serverds.getPost(Integer.parseInt(tokens[1]));
                            if (userfeed.contains(tocomment)) {
                                serverds.commentPost(user, Integer.parseInt(tokens[1]), tokens[2]);
                                response = "OK";
                            } else {
                                if (verbose)
                                    System.out.println(Colors.RED + "RequestHandler: Post not in User's Feed" + Colors.RESET);
                                response = "DENIED";
                            }
                        } catch (NumberFormatException e) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: Invalid Parameter Types" + Colors.RESET);
                            response = "INVALIDPARAMS";
                        } catch (InvalidPostIdException e) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: Invalid PostID" + Colors.RESET);
                            response = "INVALIDPOSTID";
                        } catch (InvalidUserException e) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Invalid User" + Colors.RESET);
                            response = "INVALIDUSER";
                        }
                    }
                    case "wallet" -> {
                        if (tokens.length > 3) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                            response = "BADREQ";
                            break;
                        }
                        if (!alreadylogged) {
                            if (verbose)
                                System.out.println(Colors.RED + "RequestHandler: User Not Logged" + Colors.RESET);
                            response = "NOTLOGGED";
                            break;
                        }
                        Wallet userwallet = serverds.getUser(user).getWallet();
                        StringBuilder s = new StringBuilder();
                        s.append("OK\n");
                        if (tokens.length == 2 && tokens[1].equals("btc")) {
                            s.append(userwallet.getBitcoinBalance().toString()).append("\n");
                        } else {
                            s.append(userwallet.getBalance().toString()).append("\n");
                        }
                        if (userwallet.getBalance() == 0) {
                            s.append("0 Transactions registered.");
                        } else {
                            s.append("Transactions:\n");
                            for (Transaction t : userwallet.getTransactions()) {
                                s.append("\t\tTime: ").append(t.getTimestamp()).append("\n\t\tAmount: ").append(t.getAmount()).append("\n");
                            }
                        }
                        response = s.toString();
                    }
                    case "exit" -> {
                        if (tokens.length != 1) {
                            if (verbose) System.out.println(Colors.RED + "RequestHandler: Bad Request" + Colors.RESET);
                            response = "BADREQ";
                            break;
                        }
                        if (!alreadylogged) {
                            response = "OK";
                        } else {
                            ServerMain.logins.remove(user);
                            response = "OK";
                        }
                    }
                    default -> response = "UNKNOWN";
                }
                try {
                    PrintWriter writer = new PrintWriter(clientsock.getOutputStream(), true);
                    int lenghtbytes = response.getBytes().length;
                    writer.println(lenghtbytes);
                    writer.print(response);
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setLoginBool(boolean bool){
        this.alreadylogged = bool;
    }

    public String makeList(HashSet<String> users) {
            if (users == null || users.isEmpty()) {
                return "EMPTY";
            } else {
                int counter = 0;
                StringBuilder s = new StringBuilder();
                for (String c : users) {
                    if (counter == users.size()-1) {
                        s.append(c);
                    } else {
                        s.append(c).append(", ");
                        counter++;
                    }
                }
                return s.toString();
            }
    }

    public void makeResponse(StringBuilder s, Post p){
        s.append("---------------------------------------------------------------\n");
        s.append("    PostID: ").append(p.getPostid()).append("\n");
        s.append("      Date: ").append(p.getTimestamp().toString()).append("\n");
        s.append("    Author: ").append(p.getUsername()).append("\n");
        s.append("     Title: ").append(p.getTitle()).append("\n");
        s.append("   Content: ").append(p.getContent()).append("\n");
        s.append("       Ups: ").append(p.getPositiveRates()).append("\n");
        s.append("     Downs: ").append(p.getNegativeRates()).append("\n");
        s.append("---------------------------------------------------------------\n");
    }
}
