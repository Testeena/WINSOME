package Server.exception;

public class NotYourPostException extends Throwable{
    public NotYourPostException(){
        super("Cannot delete another User's Post");
    }
}
