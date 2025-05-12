package Responses;


import java.util.HashMap;

public class Response {
    private final String protocol;
    private final int statusCode;
    private final String statusMessage;
    private final HashMap<String, String> headers;
    private final String body;

    private static final String CRFL = "\r\n";

    public Response(String protocol, int statusCode, String statusMessage, HashMap<String, String> headers, String body) {
        this.protocol = protocol;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.headers = headers;
        this.body = body;
    }

    public Response(int statusCode, String statusMessage) {
        this("HTTP/1.1", statusCode, statusMessage, null, null);
    }

    public Response(int statusCode, String statusMessage, HashMap<String, String> headers, String body) {
        this("HTTP/1.1", statusCode, statusMessage, headers, body);
    }

    public String toString() {
        StringBuilder rep = new StringBuilder();
        rep.append(String.format("%s %d %s%s", protocol, statusCode, statusMessage, CRFL));

        if (headers != null) {
            headers.forEach((key, value) -> {
                rep.append(String.format("%s: %s%s", key, value, CRFL));
            });
        }

        rep.append(CRFL);

        if (body != null) {
            rep.append(body);
        }

        return rep.toString();
    }
}
