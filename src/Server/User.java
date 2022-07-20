package Server;

import java.util.HashSet;

public class User {
    private String username;
    private String password;
    private Wallet wallet;
    private HashSet<String> tags;

    public User(){

    }

    public User(String username, String password, HashSet<String> tags) {
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

    public HashSet<String> getTags(){
        return this.tags;
    }
}