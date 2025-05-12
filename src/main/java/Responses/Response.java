package Responses;


public class Response {
    private final String protocol;
    private final int statusCode;
    private final String statusMessage;

    public Response(String protocol, int statusCode, String statusMessage) {
        this.protocol = protocol;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public String toString() {
        return String.format("%s %d %s\r\n\r\n", protocol, statusCode, statusMessage);
    }
}
