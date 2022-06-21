package Server;

import java.util.ArrayList;

public class User {
    private String username;
    private String password;
    private ArrayList<String> tags;
    private Wallet wallet;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
