package Server;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class Post {
    private  int postid;
    private String username;
    private String title;
    private String content;
    private Timestamp timestamp;
    private ConcurrentHashMap<String, Vote> rates;
    private HashSet<String> rewinners;
    private HashSet<Comment> comments;
    private int rewardIterations;

    public Post(){

    }

    public Post(int postid, String username, String title, String content) {
        this.postid = postid;
        this.username = username;
        this.title = title;
        this.content = content;
        this.comments = new HashSet<>();
        this.timestamp = new Timestamp(new java.util.Date().getTime());
        this.rates = new ConcurrentHashMap<>();
        this.rewinners = new HashSet<>();
        this.rewardIterations = 1;
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

    public ConcurrentHashMap<String, Vote> getRates() {
        return rates;
    }

    public int getPositiveRates(){
        int c = 0;
        for(Vote i : rates.values()){
            if(i.getRate() > 0) c += i.getRate();
        }
        return c;
    }

    public int getNegativeRates(){
        int c = 0;
        for(Vote i : rates.values()){
            if(i.getRate() < 0) c += i.getRate();
        }
        return c;
    }

    public Boolean ifModifiedSince (Timestamp timestamp){
        for(Vote vote : this.rates.values()){
            if(vote.getTimestamp().after(timestamp)){
                return true;
            }
        }
        for(Comment comment : this.getComments()){
            if(comment.getTimestamp().after(timestamp)){
                return true;
            }
        }
        return false;
    }

    public HashSet<String> getRewinners() {
        return rewinners;
    }

    public HashSet<Comment> getComments() {
        return comments;
    }

    public int iterateReward(){
        return this.rewardIterations++;
    }

    public void addVote(Vote vote){
        this.rates.putIfAbsent(vote.getAuthor(), vote);
    }

    public void addComment(String username, String content){
        this.comments.add(new Comment(username, content));
    }

    public void addRewinner(String username){
        this.rewinners.add(username);
    }
}
