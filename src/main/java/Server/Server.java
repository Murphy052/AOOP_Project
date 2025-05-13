package Server;

import Server.Responses.NotFound;
import Server.Responses.Response;
import Server.types.Endpoints;
import Server.types.MethodRoutePair;
import Server.types.View;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final Endpoints endpoints;

    private Server(Builder builder) {
        this.serverSocket = builder.serverSocket;
        this.threadPool = builder.threadPool;
        this.endpoints = builder.endpoints;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ServerSocket serverSocket;
        private ExecutorService threadPool;
        private Endpoints endpoints;

        public Builder port(int port) throws IOException {
            serverSocket = new ServerSocket(port);

            return this;
        }

        public Builder threads(int threads) {
            if (threads < 1) {
                throw new IllegalArgumentException("threads must be greater than 0");
            }
            threadPool = Executors.newFixedThreadPool(threads);
            return this;
        }

        public Builder addEndpoints(Endpoints endpoints) {
            this.endpoints = endpoints;

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
        Request request = new Request(client.getInputStream());

        System.out.printf("[INFO] %s: %s%n", client.getInetAddress().getHostAddress(), request.getRequestLine());

        this.threadPool.submit(() -> {
            try {
                View view = endpoints.get(new MethodRoutePair(request.method, request.target));
                this.handleEndpoint(request, client.getOutputStream(), view);
                client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void handleEndpoint(Request request, OutputStream responseOutputStream, View view) throws IOException {
        if (view == null) sendResponse(new NotFound(), responseOutputStream);

        Response resp = view.apply(request);
        String body = resp.getBody();

        if (body != null) {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "plain/text");
            headers.put("Content-Length", String.valueOf(body.length()));

            resp.addHeaders(headers);
        }

        // Send Response
        sendResponse(resp, responseOutputStream);
    }

    private static void sendResponse(Response response, OutputStream outputStream) throws IOException {
        outputStream.write(response.toString().getBytes());
    }
}
