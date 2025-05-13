package Server;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Request {
    public final String method;
    public final String target;
    public final String version;
    public final HashMap<String, String> headers;
    public final HashMap<String, String> parameters;
    public final String body;

    public Request(BufferedReader in) throws IOException {
        String startLine = in.readLine();
        if (startLine == null) throw new IOException("Start line is empty");
        String[] tokens = startLine.split(" ");
        method = tokens[0];
        target = (tokens[1].contains("?")) ? tokens[1].split("\\?")[0] : tokens[1];
        version = tokens[2];

        // Parse Headers
        HashMap<String, String> headers = new HashMap<>();
        String header = in.readLine();

        while (header != null && header.contains(": ")) {
            headers.put(header.split(": ")[0], header.split(": ")[1]);
            header = in.readLine();
        }

        this.headers = headers;
        this.parameters = parseQueryParams(tokens[1]);

        // Get Body of POST
        if (method.compareTo("POST") == 0) {
            int contentLength = 0;

            if (this.headers.containsKey("Content-Length")) {
                contentLength = Integer.parseInt(this.headers.get("Content-Length"));
            }

            char[] buffer = new char[contentLength];
            int read = 0;
            while (read < contentLength) {
                int r = in.read(buffer, read, contentLength - read);
                if (r == -1) break;
                read += r;
            }

            this.body = new String(buffer, 0, read);
        } else {
            this.body = null;
        }
    }

    public Request(InputStream in) throws IOException {
        this(new BufferedReader(new InputStreamReader(in)));
    }

    public String getRequestLine() {
        return String.format("%s %s %s", method, target, version);
    }

    private static HashMap<String, String> parseQueryParams(String target) {
        HashMap<String, String> params = new HashMap<>();
        if (target.contains("?")) {
            String paramsString = target.split("\\?")[1];
            for (String param : paramsString.split("&")) {
                params.put(param.split("=")[0], param.split("=")[1]);
            }
        }
        return params;
    }

    public JSONObject getBodyAsJSONObject() throws JSONException {
        if (this.body == null || this.headers.get("Content-Type").compareTo("application/json") != 0) return null;

        return new JSONObject(this.body);
    }
}
