package Server;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class Post {
    private final int postid;
    private final String username;
    private final String title;
    private final String content;
    private final Timestamp timestamp;
    private final ConcurrentHashMap<String, Integer> votes;
    private final HashSet<String> rewinners;
    private final HashSet<Comment> comments;

    public Post(int postid, String username, String title, String content) {
        this.postid = postid;
        this.username = username;
        this.title = title;
        this.content = content;
        this.comments = new HashSet<>();
        this.timestamp = new Timestamp(new java.util.Date().getTime());
        this.votes = new ConcurrentHashMap<>();
        this.rewinners = new HashSet<>();
    }

    public int getPostid() {
        return postid;
    }

    public String getUsername() {
        return username;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public ConcurrentHashMap<String, Integer> getRates() {
        return votes;
    }

    public HashSet<String> getRewinners() {
        return rewinners;
    }

    public HashSet<Comment> getComments() {
        return comments;
    }
    public void addVote(String username, int vote){
        this.votes.putIfAbsent(username, vote);
    }

    public void addComment(String username, String content){
        this.comments.add(new Comment(username, content));
    }

    public void addRewinner(String username){
        this.rewinners.add(username);
    }
}
