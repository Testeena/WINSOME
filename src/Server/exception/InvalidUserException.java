package Server.exception;

public class InvalidUserException extends Throwable{
    public InvalidUserException(){
        super("Invalid username");
    }
}
