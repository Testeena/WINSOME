package Server;

import Server.exception.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerDS {
    private AtomicInteger postcounter;
    private ConcurrentHashMap<String, User> users;
    private ConcurrentHashMap<Integer, Post> posts;
    private ConcurrentHashMap<String, HashSet<String>> followers;
    private ConcurrentHashMap<String, HashSet<String>> following;

    public ServerDS(){
        this.postcounter = new AtomicInteger();
        this.users = new ConcurrentHashMap<>();
        this.posts = new ConcurrentHashMap<>();
    }

    public void addUser(User newuser){
        users.putIfAbsent(newuser.getUsername(), newuser);
        followers.putIfAbsent(newuser.getUsername(), new HashSet<>());
        following.putIfAbsent(newuser.getUsername(), new HashSet<>());
    }

    public User getUser(String username){
        return users.get(username);
    }

    public ArrayList<String> getUserFeed(User u){
        ArrayList<String> res = new ArrayList<>();
        res.addAll(following.get(u.getUsername()));
        for(User i : users.values()){
            for(String tag : u.getTags()){
                if(i.getTags().contains(tag) && !i.getUsername().equals(u.getUsername()) && !res.contains(i.getUsername())){
                    res.add(i.getUsername());
                }
            }
        }
        return res;
    }

    public void follow(String username1, String username2) throws InvalidUserException{
        if(users.containsKey(username1) && users.containsKey(username2)){
            followers.get(username1).add(username2);
            following.get(username2).add(username1);
        }
        else{
            throw new InvalidUserException();
        }
    }

    public void unfollow(String username1, String username2) throws InvalidUserException{
        if(users.containsKey(username1) && users.containsKey(username2)){
            followers.get(username1).remove(username2);
            following.get(username2).remove(username1);
        }
        else{
            throw new InvalidUserException();
        }
    }

    public HashSet<String> getFollowers(String username) throws InvalidUserException {
        if(users.containsKey(username)){
            return followers.get(username);
        }
        else{
            throw new InvalidUserException();
        }
    }

    public HashSet<String> getFollowing(String username) throws InvalidUserException {
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

    public void votePost(int postid, String username, int rate) throws InvalidPostIdException, InvalidUserException, AlreadyVotedException, InvalidVoteException {
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
            got.addVote(username, +1);
        }
        else if(rate < 0){
            got.addVote(username, -1);
        }
        else{
            throw new InvalidVoteException();
        }
    }

    public void commentPost(int postid, String username, String content) throws InvalidPostIdException, InvalidUserException {
        if(!posts.containsKey(postid)){
            throw new InvalidPostIdException();
        }
        if(!users.containsKey(username)){
            throw new InvalidUserException();
        }
        posts.get(postid).addComment(username, content);
    }

    public void rewinPost(int postid, String username) throws InvalidPostIdException, InvalidUserException {
        if(!posts.containsKey(postid)){
            throw new InvalidPostIdException();
        }
        if(!users.containsKey(username)){
            throw new InvalidUserException();
        }
        Post got = posts.get(postid);
        got.addRewinner(username);
        createPost(username, got.getTitle(), "Rewinned from @" + got.getUsername() + "\n" + got.getContent());
    }


}
