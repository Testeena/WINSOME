package Server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class User {
    private final String username;
    private final String password;
    private final Wallet wallet;
    private final ArrayList<String> tags;

    public User(String username, String password, ArrayList<String> tags) {
        this.username = username;
        this.password = password;
        this.wallet = new Wallet();
        this.tags = tags;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Wallet getWallet(){
        return this.wallet;
    }

    public ArrayList<String> getTags(){
        return this.tags;
    }
}