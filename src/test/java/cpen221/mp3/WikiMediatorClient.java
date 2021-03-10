package cpen221.mp3.server;

import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;

public class WikiMediatorClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    // Rep invariant: socket, in, out != null

    /**
     * Make a FibonacciClient and connect it to a server running on
     * hostname at the specified port.
     * @throws IOException if can't connect
     */
    public WikiMediatorClient(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    /**
     * Send a request to the server. Requires this is "open".
     * @param question to send request
     * @throws IOException if network or server failure
     */
    public void sendRequest(JsonObject question) throws IOException {
        out.print(question + "\n");
        out.flush();
    }

    /**
     * Get a reply from the next request that was submitted.
     * Requires this is "open".
     * @return the requested Fibonacci number
     * @throws IOException if network or server failure
     */
    public String getReply() throws IOException {
        String reply = in.readLine();
        if (reply == null) {
            throw new IOException("connection terminated unexpectedly");
        }

        try {
            return reply;
        } catch (Exception e) {
            throw new IOException("misformatted reply: " + reply);
        }
    }

    /**
     * Closes the client's connection to the server.
     * This client is now "closed". Requires this is "open".
     * @throws IOException if close fails
     */
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }


    /**
     * Use a FibonacciServer to find the first N Fibonacci numbers.
     */
    public static void main(String[] args) {
        try {
            WikiMediatorClient client = new WikiMediatorClient("localhost", 4949);

            // send the requests
            JsonObject j1 = new JsonObject();
            j1.addProperty("id","1");
            j1.addProperty("type", "simpleSearch");
            j1.addProperty("query", "Barack Obama");
            j1.addProperty("limit", 12);
            client.sendRequest(j1);
            System.out.println("simpleSearch");

            // collect the replies
            String answer = client.getReply();
            System.out.println(answer);

            client.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
