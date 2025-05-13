package Server.Responses;

public class Ok extends Response{
    public Ok() {
        super(200, "OK");
    }

    public Ok(String message) {
        super(200, message);
    }
}
