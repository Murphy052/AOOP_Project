import Server.Server;


public class Main {
    public static void main(String[] args) {
        // start server
        try {
            Server server = Server.builder()
                    .port(4221)
                    .threads(4)
                    .addEndpoints(Router.getEndpoints()).build();

            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
