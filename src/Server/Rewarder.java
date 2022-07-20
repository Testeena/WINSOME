package Server;

import Server.exception.InvalidUserException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class Rewarder implements Runnable{
    ServerDS ds;
    SConfigs serverconfigs;
    Timestamp lastupdatetime;
    private Boolean keepWorking;

    public Rewarder(ServerDS ds, SConfigs serverconfigs){
        this.ds = ds;
        this.serverconfigs = serverconfigs;
        this.lastupdatetime = new Timestamp(new java.util.Date().getTime());
        this.keepWorking = true;
    }

    @Override
    public void run() {
        try (DatagramSocket sock = new DatagramSocket(7777)){

            ConcurrentHashMap<Integer, Post> tempposts;
            while(keepWorking){
                tempposts = ds.getPosts();
                double reward = 0;
                double temp = 0;

                try{
                    Thread.sleep(serverconfigs.rewardTimer);
                }catch (InterruptedException e){
                    if(!keepWorking){
                        break;
                    }
                }
                //System.out.println(Colors.GREEN + "------ $ Generating Rewards $ ------" + Colors.RESET);
                if(!tempposts.isEmpty()){
                    for(Post post : tempposts.values()){
                        if(post.ifModifiedSince(lastupdatetime)){
                            try{
                                temp = generateReward(post);
                            } catch (InvalidUserException e){
                                System.err.println(Colors.RED + "Invalid user in Rewarder" + Colors.RESET);
                                continue;
                            }
                            reward += temp;
                        }
                    }
                    if(reward != 0){
                        String rewardmessage = String.valueOf(reward);
                        InetAddress address = InetAddress.getByName(serverconfigs.mcastAddress);
                        ByteBuffer lenght = ByteBuffer.allocate(Double.BYTES);
                        lenght.putDouble(rewardmessage.getBytes().length);
                        DatagramPacket lenghtpacket = new DatagramPacket(lenght.array(), lenght.limit(), address, serverconfigs.mcastPort);
                        sock.send(lenghtpacket);
                        ByteBuffer rewardbytes = ByteBuffer.allocate(rewardmessage.getBytes().length);
                        rewardbytes.put(rewardmessage.getBytes());
                        DatagramPacket rewardPacket = new DatagramPacket(rewardbytes.array(), rewardbytes.limit(), address, serverconfigs.mcastPort);
                        sock.send(rewardPacket);
                    }
                }
            }
        } catch (IOException e){
            System.err.println(Colors.RED + "Rewarder Thread error" + Colors.RESET);
        }
    }

    private double generateReward(Post p) throws InvalidUserException {
        double total = 0, ratesSum = 0, commentsSum = 0;
        int niteration = p.iterateReward();
        Timestamp temp = this.lastupdatetime;
        this.lastupdatetime = new Timestamp(new java.util.Date().getTime());

        HashMap<String, Integer> voters = new HashMap<>();
        for(Vote vote : p.getRates().values()){
            if(vote.getTimestamp().after(temp)){
                ratesSum += vote.getRate();
                voters.putIfAbsent(vote.getAuthor(), vote.getRate());
            }
        }
        if(ratesSum <= 0){
            ratesSum = 1;
        }
        else{
            ratesSum++;
        }

        HashMap<String, Integer> commentators = new HashMap<>();
        for(Comment comment : p.getComments()){
            if(comment.getTimestamp().after(temp)){
                if(commentators.containsKey(comment.getUsername())){
                    commentators.replace(comment.getUsername(), commentators.get(comment.getUsername()) + 1);
                }
                else{
                    commentators.put(comment.getUsername(), 1);
                }
            }
        }

        int commentcount = 0;
        for(String user : commentators.keySet()){
            commentcount = commentators.get(user);
            commentsSum += 2/(1 + Math.pow(Math.E, -(commentcount-1)));
        }
        commentsSum++;
        total = (Math.log(ratesSum)+Math.log(commentsSum))/niteration;

        if(total != 0){
            ds.getUser(p.getUsername()).getWallet().addTransaction(total * ((float)serverconfigs.authorShare/100));
            HashSet<String> rewarded = new HashSet<>();
            rewarded.addAll(voters.keySet());
            rewarded.addAll(commentators.keySet());
            rewarded.remove(p.getUsername());

            if(!rewarded.isEmpty()){
                double reward = total * ((float)(100 - serverconfigs.authorShare) / 100)/rewarded.size();
                for(String user : rewarded){
                    ds.getUser(user).getWallet().addTransaction(reward);
                }
            }
        }
        return total;
    }

    public void stopWorking(){
        this.keepWorking = false;
    }
}
