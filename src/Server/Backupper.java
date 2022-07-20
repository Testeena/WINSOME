package Server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

public class Backupper implements Runnable{

    private ServerDS backupDS;
    private long backupTimer = 5000;
    private final String userspath = "src/backup/users.json";
    private final String postspath = "src/backup/posts.json";
    private final String followerspath = "src/backup/followers.json";
    private final String followingpath = "src/backup/following.json";

    public Backupper(ServerDS serverds, long backupTimer){
        this.backupDS = serverds;
        this.backupTimer = backupTimer;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()){
            try{
                Thread.sleep(backupTimer);
            } catch (InterruptedException e) {
                return;
            }
            if(!Thread.currentThread().isInterrupted()){
                makeBackup();
            }
        }
        makeBackup();
    }

    private void makeBackup(){
        JsonFactory jsonfactory = new JsonFactory();
        ObjectMapper objectmapper = new ObjectMapper(jsonfactory);
        objectmapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectmapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectmapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try{
            //System.out.println(Colors.BLUE + "------  Saving backup  ------" + Colors.RESET);
            objectmapper.writeValue(new File(userspath), backupDS.getUsers());
            objectmapper.writeValue(new File(postspath), backupDS.getPosts());
            objectmapper.writeValue(new File(followerspath), backupDS.getFollowers());
            objectmapper.writeValue(new File(followingpath), backupDS.getFollowing());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
