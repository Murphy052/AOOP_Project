import Responses.NotFound;
import Responses.Ok;
import Responses.Response;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    private final ServerSocket serverSocket;

    private Server(Builder builder) {
        this.serverSocket = builder.serverSocket;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int threads = 4;
        private ServerSocket serverSocket;
        private ThreadPoolExecutor executor;

        public Builder port(int port) throws IOException {
            serverSocket = new ServerSocket(port);

            return this;
        }

        public Builder threads(int threads) {
            if (threads < 1) {
                throw new IllegalArgumentException("threads must be greater than 0");
            }

            return this;
        }

        public Server build() {
            return new Server(this);
        }
    }

    public void start() throws IOException {
        this.serverSocket.setReuseAddress(true);
        this.startListening();
    }

    private void startListening() throws IOException {
        while (!this.serverSocket.isClosed()) {
            this.acceptRequest();
        }
    }

    private void acceptRequest() throws IOException {
        Socket client = this.serverSocket.accept();
        System.out.printf("Accepted new connection from: %s%n", client.getInetAddress().getHostAddress());

        Request request = this.getRequest(client.getInputStream());
        this.handleRequest(request, client.getOutputStream());
    }

    private Request getRequest(InputStream requestInputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(requestInputStream));
        String requestLine = in.readLine();

        return new Request(requestLine);
    }

    private void handleRequest(Request request, OutputStream responseOutputStream) throws IOException {
        Response resp;
        if (request.getTarget().compareToIgnoreCase("/") == 0) {
            resp = new Ok();
        } else {
            resp = new NotFound();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Send Response
        responseOutputStream.write(resp.toString().getBytes());
    }
}
