import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class proxyd {
  public static void main(String[] args) throws IOException {

    int port = Integer.parseInt(args[1]);
    ConcurrentHashMap<String,DnsEntry> dnsCache = new ConcurrentHashMap<String,DnsEntry>();

    try {


      // Print a start-up message
      // System.out.println("Starting the HTTP Proxy Server");

      // And start running the server
      runServer(port, dnsCache); // never returns

    } catch (Exception e) {

      System.err.println(e);

    }
  }


  // Runs the HTTP server, accepting requests on port 80 and creating threads to handle them
  public static void runServer(int port, ConcurrentHashMap<String,DnsEntry> dnsCache)
      throws IOException {

    // Create a ServerSocket to listen for connections
    // Taking 325, student number 45 by alphabetical ordering on Canvas students list
    // Port number should be 5045

    // Create a server socket to accept requests    
    ServerSocket ss = new ServerSocket(port);

    // Socket to hold created sockets handling incoming connections
    Socket client = null;

    while (true) {

      try {
        
        // Wait for a connection on the local port
        client = ss.accept();

        // Hand off the socket to a new thread
        new RequestHandler(client, dnsCache).start();

      } catch (IOException e) {
        System.err.println(e);
      }
    }
  }
}
