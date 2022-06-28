package Server.exception;

public class AlreadyVotedException extends Throwable{
    public AlreadyVotedException(){
        super("Cannot vote an already voted post");
    }
}
