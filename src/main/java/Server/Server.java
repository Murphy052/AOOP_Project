package Server;

import Server.Responses.NotFound;
import Server.Responses.Response;
import Server.types.Endpoints;
import Server.types.MethodRoutePair;
import Server.types.View;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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

        this.threadPool.submit(() -> {
            try {
                this.handleRequest(client);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void handleRequest(Socket client) throws IOException {
        Request request = new Request(client.getInputStream());

        System.out.printf("[INFO] %s: %s%n", client.getInetAddress().getHostAddress(), request.getRequestLine());

        if ("/subscribe".equals(request.target)) {
            Response response = WebsocketMulticast.registerClient(client, request);
            sendResponse(response, client.getOutputStream());
            return;
        }

        View view = endpoints.get(new MethodRoutePair(request.method, request.target));
        Response response = this.handleEndpoint(request, view);
        sendResponse(response, client.getOutputStream());

        client.close();
    }

    private Response handleEndpoint(Request request, View view) throws IOException {
        if (view == null) return new NotFound();

        Response resp = view.apply(request);
        String body = resp.getBody();

        if (body != null) {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "plain/text");
            headers.put("Content-Length", String.valueOf(body.length()));

            resp.addHeaders(headers);
        }

        return resp;
    }

    private static void sendResponse(Response response, OutputStream outputStream) throws IOException {
        outputStream.write(response.toString().getBytes(StandardCharsets.UTF_8));
    }
}
