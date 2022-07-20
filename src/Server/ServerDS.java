package Server;

import Server.exception.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerDS{

    private static final File userjson = new File("src/backup/users.json");
    private static final File postsjson = new File("src/backup/posts.json");
    private static final File followerjson = new File("src/backup/followers.json");
    private static final File followingjson = new File("src/backup/following.json");

    private AtomicInteger postcounter;
    private ConcurrentHashMap<String, User> users;
    private ConcurrentHashMap<Integer, Post> posts;
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> followers;
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> following;

    public ServerDS(){
        this.postcounter = new AtomicInteger();
        this.users = new ConcurrentHashMap<>();
        this.posts = new ConcurrentHashMap<>();
        this.followers = new ConcurrentHashMap<>();
        this.following = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, User> getUsers() {
        return users;
    }

    public ConcurrentHashMap<Integer, Post> getPosts() {
        return posts;
    }

    public ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> getFollowers() {
        return followers;
    }

    public ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> getFollowing() {
        return following;
    }

    public void addUser(User newuser){
        users.putIfAbsent(newuser.getUsername(), newuser);
        followers.putIfAbsent(newuser.getUsername(), new ConcurrentLinkedQueue<>());
        following.putIfAbsent(newuser.getUsername(), new ConcurrentLinkedQueue<>());
    }

    public Boolean contains(String username){
        if(users.containsKey(username)){
            return true;
        }
        else{
            return false;
        }
    }

    public User getUser(String username){
        return users.get(username);
    }

    public HashSet<String> getSameTags(User u) throws InvalidUserException{
        if(!users.containsKey(u.getUsername())){
            throw new InvalidUserException();
        }
        HashSet<String> res = new HashSet<>();
        for(User i : users.values()){
            for(String tag : u.getTags()){
                if(i.getTags().contains(tag) && !i.getUsername().equals(u.getUsername())){
                    res.add(i.getUsername());
                }
            }
        }
        return res;
    }

    public HashSet<Post> getUserFeed(String u) throws InvalidUserException {
        if(!users.containsKey(u)){
            throw new InvalidUserException();
        }
        User user = users.get(u);
        HashSet<Post> res = new HashSet<>();
        HashSet<String> temp = getSameTags(user);

        for(Post p : posts.values()){
            if(following.get(u).contains(p.getUsername())){
                res.add(p);
            }
            if(temp.contains(p.getUsername())){
                res.add(p);
            }
        }
        return res;
    }

    public HashSet<Post> getUserBlog(String u) throws InvalidUserException {
        if(!users.containsKey(u)){
            throw new InvalidUserException();
        }
        HashSet<Post> res = new HashSet<>();
        for(Post i : posts.values()){
            if(i.getUsername().equals(u)) {
                res.add(i);
            }
        }
        return res;
    }

    public void follow(String username1, String username2) throws InvalidUserException{
        if(users.containsKey(username1) && users.containsKey(username2)){
            try {
                if(following.get(username1).contains(username2)){
                    return;
                }
                following.get(username1).add(username2);
                followers.get(username2).add(username1);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            throw new InvalidUserException();
        }
    }

    public void unfollow(String username1, String username2) throws InvalidUserException{
        if(users.containsKey(username1) && users.containsKey(username2)){
            try {
                if(!following.get(username1).contains(username2)){
                    return;
                }
                following.get(username1).remove(username2);
                followers.get(username2).remove(username1);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            throw new InvalidUserException();
        }
    }

    public ConcurrentLinkedQueue<String> getFollowers(String username) throws InvalidUserException {
        if(users.containsKey(username)){
            return followers.get(username);
        }
        else{
            throw new InvalidUserException();
        }
    }

    public ConcurrentLinkedQueue<String> getFollowing(String username) throws InvalidUserException {
        if(users.containsKey(username)){
            return following.get(username);
        }
        else{
            throw new InvalidUserException();
        }
    }

    public void createPost(String username, String title, String content) throws InvalidUserException {
        if(users.containsKey(username)){
            int postid = postcounter.incrementAndGet();
            posts.putIfAbsent(postid, new Post(postid, username, title, content));
        }
        else{
            throw new InvalidUserException();
        }
    }

    public void deletePost(String username, int postid) throws InvalidUserException, InvalidPostIdException, NotYourPostException {
        if(!users.containsKey(username)){
            throw new InvalidUserException();
        }
        if(!posts.containsKey(postid)){
            throw new InvalidPostIdException();
        }
        if(posts.get(postid).getUsername().equals(username)){
           posts.remove(postid);
        }
        else{
            throw new NotYourPostException();
        }
    }

    public Post getPost(int postid) throws InvalidPostIdException {
        if(posts.containsKey(postid)){
            return posts.get(postid);
        }
        else{
            throw new InvalidPostIdException();
        }
    }

    public void votePost(String username, int postid,  int rate) throws InvalidPostIdException, InvalidUserException, AlreadyVotedException, InvalidVoteException {
        if(!posts.containsKey(postid)){
            throw new InvalidPostIdException();
        }
        if(!users.containsKey(username)){
            throw new InvalidUserException();
        }
        Post got = getPost(postid);
        if(got.getRates().containsKey(username)){
            throw new AlreadyVotedException();
        }

        if(rate >= 1){
            got.addVote(new Vote(username, +1));
        }
        else if(rate < 0){
            got.addVote(new Vote(username, -1));
        }
        else{
            throw new InvalidVoteException();
        }
    }

    public void commentPost(String username, int postid, String content) throws InvalidPostIdException, InvalidUserException {
        if(!posts.containsKey(postid)){
            throw new InvalidPostIdException();
        }
        if(!users.containsKey(username)){
            throw new InvalidUserException();
        }
        posts.get(postid).addComment(username, content);
    }

    public void rewinPost(String username, int postid) throws InvalidPostIdException, InvalidUserException {
        if(!posts.containsKey(postid)){
            throw new InvalidPostIdException();
        }
        if(!users.containsKey(username)){
            throw new InvalidUserException();
        }
        Post got = posts.get(postid);
        got.addRewinner(username);
        createPost(username, got.getTitle(), "Rewinned from @" + got.getUsername() + "\n\t\t\t" + got.getContent());
    }

    private int getMaxPostCounter(){
        int toret = 0;
        for(Map.Entry<Integer, Post> i: this.posts.entrySet()){
            int temp = i.getKey();
            if (toret < temp){
                toret = temp;
            }
        }
        return toret;
    }

    public void loadBackup(){
        try{
            if(!userjson.exists() || !postsjson.exists() || !followerjson.exists() || !followingjson.exists()){
                System.err.println("Backup wasn't possible due to not existing files." );
            }
            else{
                ObjectMapper objectmapper = new ObjectMapper();
                objectmapper.enable(SerializationFeature.INDENT_OUTPUT);
                objectmapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
                objectmapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
                //objectmapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

                System.out.println(Colors.YELLOW + "Loading json Backups into Internal Datastructures" + Colors.RESET);
                BufferedReader reader = new BufferedReader(new FileReader(userjson));
                try {
                    this.users = objectmapper.readValue(reader, new TypeReference<ConcurrentHashMap<String, User>>(){});

                    reader = new BufferedReader(new FileReader(postsjson));
                    this.posts = objectmapper.readValue(reader, new TypeReference<ConcurrentHashMap<Integer, Post>>(){});
                    this.postcounter.getAndAdd(getMaxPostCounter());

                    reader = new BufferedReader(new FileReader(followerjson));
                    this.followers = objectmapper.readValue(reader, new TypeReference<ConcurrentHashMap<String, ConcurrentLinkedQueue<String>>>(){});

                    reader = new BufferedReader(new FileReader(followingjson));
                    this.following = objectmapper.readValue(reader, new TypeReference<ConcurrentHashMap<String, ConcurrentLinkedQueue<String>>>(){});
                } catch (MismatchedInputException ignore){
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
