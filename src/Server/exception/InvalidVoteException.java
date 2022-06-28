package Server.exception;

public class InvalidVoteException extends Throwable{
    public InvalidVoteException(){
        super("Invalid Vote");
    }
}
