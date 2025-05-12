package Responses;

public class NotFound extends Response {
    public NotFound() {
        super(404, "Not Found");
    }

    public NotFound(String message) {
        super(404, message);
    }
}
