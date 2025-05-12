package Responses;

public class NotFound extends Response {
    public NotFound() {
        super("HTTP/1.1", 404, "Not Found");
    }
}
