public class Response {
    private String protocol;
    private int statusCode;
    private String statusMessage;

    public Response() {
        protocol = "HTTP/1.1";
        statusCode = 200;
        statusMessage = "OK";
    }

    public String toString() {
        return String.format("%s %d %s\r\n", protocol, statusCode, statusMessage);
    }
}
