public class Request {
    private final String method;
    private final String target;
    private final String version;

    public Request(String s) {
        String[] tokens = s.split(" ");
        method = tokens[0];
        target = tokens[1];
        version = tokens[2];
    }

    public String getTarget() {
        return target;
    }
}
