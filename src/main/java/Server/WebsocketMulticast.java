package Server;

import Server.Responses.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public class WebsocketMulticast {
    private static final ArrayList<Socket> clients = new ArrayList<>();

    public static synchronized Response registerClient(Socket clientSocket, Request request) {
        try {
            String clientKey = request.headers.get("Sec-WebSocket-Key");

            if (clientKey == null) {
                clientSocket.close();
                return new Response(400, "Bad Request");
            }

            String acceptKey = calculateAcceptKey(clientKey);

            clients.add(clientSocket);

            return craftResponse(acceptKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Response(500, "Internal Server Error");
    }

    public static synchronized void notifyAll(Object message) throws IOException {
        byte[] bytes = message.toString().getBytes();
        for (Socket client : clients) {
            if (client != null) {
                sendText(client.getOutputStream(), bytes);
            }
        }
    }

    private static void sendText(OutputStream out, byte[] byteMessage) throws IOException {
        int frameLen = byteMessage.length;

        out.write(0x81); // FIN=1, text frame
        if (frameLen <= 125) {
            out.write(frameLen);
        }

        out.write(byteMessage);
        out.flush();
    }

    private static String calculateAcceptKey(String clientKey) throws Exception {
        String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

        return base64Encode(sha1(clientKey + magicString));
    }

    private static byte[] sha1(String input) throws Exception {
        var md = MessageDigest.getInstance("SHA-1");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    private static String base64Encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private static Response craftResponse(String acceptKey) throws IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Upgrade", "websocket");
        headers.put("Connection", "Upgrade");
        headers.put("Sec-WebSocket-Accept", acceptKey);

        Response response = new Response(101, "Switching Protocols");
        response.addHeaders(headers);

        return response;
    }
}
