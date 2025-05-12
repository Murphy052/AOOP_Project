import Responses.Response;
import Responses.Ok;

public class Main {
    public static void main(String[] args) {
        try {

            Server server = Server.builder().port(4221).build();
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Response root() {
        return new Ok();
    }
}
