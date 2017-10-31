import java.io.*;
import java.net.*;
import java.util.*;
import java.time.*;
import java.util.concurrent.*;


public class RequestHandler extends Thread{

  private Socket client;
  private ConcurrentHashMap<String,DnsEntry> dnsCache;

  public RequestHandler(Socket socket, ConcurrentHashMap<String,DnsEntry> dnsCache){

    this.client = socket;
    this.dnsCache = dnsCache;

  }
  
  public void run() {

    try {

      byte[] request = new byte[2048];
      byte[] reply = new byte[2048];
      int bytes_read;

      final InputStream streamFromClient = client.getInputStream();
      final OutputStream streamToClient = client.getOutputStream();

      // Read the contents of the InputStream into request.
      while ((bytes_read = streamFromClient.read(request)) != -1){

        // Extract the header

        String str_header = getHeader(request);

        // Break up the header into its component parts
        ArrayList<String> header_parts = new ArrayList<String>(Arrays.asList(str_header.split("\r\n")));

        // Determine the IP address of the host. 
        // First check the DNS Cache to see if it is present and less than 30 seconds old. If not, do a DNS lookup.

        String[] request_parts = header_parts.get(0).split(" ");

        String host = parse_URL_for_host(request_parts[1]);

        String address;

        InetAddress addr;

        DnsEntry entry;

        // Is the hostname in the cache?
        if (dnsCache.containsKey(host)){

          // If the DNS entry in the cache still live (less than 30 seconds old)?
          if ((entry = dnsCache.get(host)).isLive(Instant.now().toEpochMilli())){

            // If so, just grab the cached address
            address = entry.getAddress();
            //System.out.println("Got a cached address.");

          // If not
          } else{

            // Perform a DNS lookup
            addr = InetAddress.getByName(host);
            address = addr.getHostAddress();

            // Remove the old entry from the cache and add a new entry with the just produced values and the current time
            dnsCache.remove(host);
            dnsCache.put(host, new DnsEntry(host, address, Instant.now().toEpochMilli()));
            //System.out.println("Updated a cached address.");

          }
        // If not
        } else {

          // Perform a DNS lookup
          addr = InetAddress.getByName(host);
          address = addr.getHostAddress();

          // Add a new entry to the cache with the just produced values and the current time
          dnsCache.put(host, new DnsEntry(host, address, Instant.now().toEpochMilli()));
        }

        // Update the request line to correctly not include the host

        String[] updated_request_parts = update_request_parts(request_parts);

        String request_line = String.join(" ", updated_request_parts);

        header_parts.set(0, request_line);

        // Replace either a Connection or a Proxy-connection field with "Connection: close" , or add the field if it does not exist
        // Used to avoid blocking on reads

        int connection_field_index = get_connection_field(header_parts);

        // If there was a connection field, update it
        if (connection_field_index != -1) {
          header_parts.set(connection_field_index, "Connection: close");
        }
        // Else add a connection field
        else {
          header_parts.add("Connection: close");
        }

        // Convert the header back into bytes and append the delimiter and original message body to it.

        String updated_header = String.join("\r\n", header_parts.toArray(new String[0]));

        byte[] byte_header = updated_header.getBytes("ASCII");
        byte[] byte_body = getBody(request, bytes_read);
        byte[] delimiter = {(byte) 13, (byte) 10, (byte) 13, (byte) 10};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(byte_header);
        outputStream.write(delimiter);
        outputStream.write(byte_body);
        byte[] modified_request = outputStream.toByteArray();

        // Open a socket to the host on port 80 and create input/output streams

        Socket server = new Socket(address, 80);
        final InputStream streamFromServer = server.getInputStream();
        final OutputStream streamToServer = server.getOutputStream();

        // Send the modified request to the server

        streamToServer.write(modified_request);
        streamToServer.flush();

        // Read responses from the server until the server closes the connection
        while ((bytes_read = streamFromServer.read(reply)) != -1){
          // Send those responses unmodified to the client
          streamToClient.write(reply, 0, bytes_read);
          streamToClient.flush();
        }
      }
    } catch (IOException e) {
        System.err.println(e);
    }
    return;
  }


  // Returns the header of a message
  public static String getHeader(byte[] message){

    byte[] delimiter = {(byte) 13, (byte) 10, (byte) 13, (byte) 10};

    int cutoff = firstIndexOf(message, delimiter);

    byte[] header = Arrays.copyOfRange(message, 0, cutoff);

    return new String(header);

  }

  // Returns the body of a message
  public static byte[] getBody(byte[] message, int original_length){

    byte[] delimiter = {(byte) 13, (byte) 10, (byte) 13, (byte) 10};

    int cutoff = firstIndexOf(message, delimiter);

    byte[] body = Arrays.copyOfRange(message, cutoff + 4, original_length);

    return body;

  }

  // Returns the first index of a byte array in a second byte array, or -1 if it is not present
  public static int firstIndexOf(byte[] outerArray, byte[] smallerArray) {
    for(int i = 0; i < outerArray.length - smallerArray.length+1; ++i) {
      boolean found = true;
      for(int j = 0; j < smallerArray.length; ++j) {
       if (outerArray[i+j] != smallerArray[j]) {
         found = false;
         break;
       }
     }
     if (found) return i;
   }
   return -1;  
  }

  // Takes a URL string and returns the hostname
  public static String parse_URL_for_host(String url_to_parse) throws MalformedURLException{

    URL url = new URL(url_to_parse);
    String host = url.getHost();

    return host;
  }

  // Takes the original request line space-delimited and properly formats the requestline without the host, returning it.
  public static String[] update_request_parts(String[] request) throws MalformedURLException{

    URL url = new URL(request[1]);
    String path = url.getPath();
    request[1] = path;

    return request;
  }

  // Takes the list of header parts and returns the index of the connection field, or -1 if it does not exist
  public static int get_connection_field(ArrayList<String> parts){

    for (int i = 0; i < parts.size(); i++){
      String field = parts.get(i).split(" ")[0];
      if (field.equals("Connection:") || field.equals("Proxy-connection:")){
        return i;
      }
    }

    return -1;
  }
}