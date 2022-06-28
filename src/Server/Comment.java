package Server;

import java.sql.Timestamp;

public class Comment {
    private final String username;
    private final String content;
    private final Timestamp timestamp;

    public Comment(String username, String content) {
        this.username = username;
        this.content = content;
        this.timestamp = new Timestamp(new java.util.Date().getTime());
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
