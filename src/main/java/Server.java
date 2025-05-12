import Responses.NotFound;
import Responses.Ok;
import Responses.Response;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final ArrayList<Endpoint> endpoints;

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
        private ArrayList<Endpoint> endpoints;

        public Builder port(int port) throws IOException {
            serverSocket = new ServerSocket(port);

            return this;
        }

        public Builder endpoints(ArrayList<Endpoint> endpoints) {
            this.endpoints = endpoints;

            return this;
        }

        public Builder threads(int threads) {
            if (threads < 1) {
                throw new IllegalArgumentException("threads must be greater than 0");
            }
            threadPool = Executors.newFixedThreadPool(threads);
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

        this.threadPool.submit(new ServerService(client, request));
    }
}


class ServerService implements Runnable {
    private final Socket client;
    private final Request request;

    public ServerService(Socket client, Request request) {
        this.client = client;
        this.request = request;
    }

    public void run() {
        try {
            this.handleRequest(request, client.getOutputStream());
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRequest(Request request, OutputStream responseOutputStream) throws IOException {
        Response resp;
        HashMap<String, String> headers = new HashMap<>();
        String body = null;

        switch (request.target) {
            case "/":
                resp = new Ok();
                break;
            case "/body":
                body = "Hello";

                headers.put("Content-Type", "plain/text");
                headers.put("Content-Length", String.valueOf(body.length()));

                resp = new Response(200, "OK", headers, body);

                break;
            case "/user-agent":
                body = request.headers.get("User-Agent");

                headers.put("Content-Type", "plain/text");
                headers.put("Content-Length", String.valueOf(body.length()));

                resp = new Response(200, "OK", headers, body);

                break;
            default:
                resp = new NotFound();
                break;
        }

        // Send Response
        responseOutputStream.write(resp.toString().getBytes());
    }
}