package Responses;

public class Ok extends Response{
    public Ok() {
        super("HTTP/1.1", 200, "OK");
    }
}
