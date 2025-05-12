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

    public Request(BufferedReader in) throws IOException {
        String startLine = in.readLine();
        if (startLine == null) throw new IOException("Start line is empty");
        String[] tokens = startLine.split(" ");
        method = tokens[0];
        target = tokens[1];
        version = tokens[2];

        // Parse Headers
        HashMap<String, String> headers = new HashMap<>();
        String header = in.readLine();

        while (header != null && header.contains(": ")) {
            headers.put(header.split(": ")[0], header.split(": ")[1]);
            header = in.readLine();
        }

        this.headers = headers;
    }

    public Request(InputStream in) throws IOException {
        this(new BufferedReader(new InputStreamReader(in)));
    }

    public String getRequestLine() {
        return String.format("%s %s %s", method, target, version);
    }
}
