package Server.exception;

public class InvalidPostIdException extends Throwable{
    public InvalidPostIdException(){
        super("Invalid PostId");
    }
}
