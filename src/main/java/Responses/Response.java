package Responses;


public class Response {
    private final String protocol;
    private final int statusCode;
    private final String statusMessage;

    private static final String CRFL = "\r\n";

    public Response(String protocol, int statusCode, String statusMessage) {
        this.protocol = protocol;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public Response(int statusCode, String statusMessage) {
        this("HTTP/1.1", statusCode, statusMessage);
    }

    public String toString() {
        return String.format("%s %d %s%s%s", protocol, statusCode, statusMessage, CRFL, CRFL);
    }
}
