import Responses.NotFound;
import Responses.Ok;
import Responses.Response;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage

     try {
       ServerSocket serverSocket = new ServerSocket(4221);

       // Since the tester restarts your program quite often, setting SO_REUSEADDR
       // ensures that we don't run into 'Address already in use' errors
       serverSocket.setReuseAddress(true);

       Socket client = serverSocket.accept(); // Wait for connection from client.
       System.out.println("accepted new connection");

       BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
       PrintWriter out = new PrintWriter(client.getOutputStream(), true);

       String requestLine = in.readLine();

       Request request = new Request(requestLine);
       Response resp;
       if (request.getTarget().compareToIgnoreCase("/") == 0) {
         resp = new Ok();
       } else {
         resp = new NotFound();
       }

       out.println(resp);

     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
