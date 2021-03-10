package cpen221.mp3.server;

import com.google.gson.*;
import cpen221.mp3.wikimediator.WikiMediator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/** Referred to the FIbbonacci example from Network Programming notes**/
/** Also referred to ex 15 Jeopardy to deal with JSON objects **/


/**
 * RI:
 * a server that deals with requests over a network socket.
 *
 * the cache and statistical information is stored in the local filesystem,
 * and can be reloaded when the service is started.
 *
 * AF: the requests are treated as JSON strings
 */
public class WikiMediatorServer {

    /**
     * Start a server at a given port number, with the ability to process
     * upto n requests concurrently.
     *
     * @param port the port number to bind the server to
     * @param n the number of concurrent requests the server can handle
     */
    private ServerSocket serversocket;
    private int maxreq;

    public WikiMediatorServer(int port, int n) throws IOException {

        /* TODO: Implement this method */
        serversocket = new ServerSocket(port);
        maxreq = n;
    }


    /**
     * Run the server, listening for connections and handling them.
     *
     * @throws java.io.IOException if the main server socket is broken
     */

    public void serve() throws IOException {
        int req = 0;
        while (req<maxreq) {
            //block intil a client connects
            final Socket socket = serversocket.accept();
            //create a new thread to handle the client

            Thread handler = new Thread(new Runnable() {
                public void run() {
                    try {
                        try {
                            handle(socket);
                        } finally {
                            socket.close();
                        }
                    } catch (IOException ioe) {
                        // this exception wouldn't terminate serve(),
                        // since we're now on a different thread, but
                        // we still need to handle it
                        ioe.printStackTrace();
                    }
                }
            });
            // start the thread
            handler.start();
            req++;
        }

    }

    /**
     * Handle one client connection. Returns when client disconnects.
     *
     * @param socket socket where client is connected
     * @throws IOException if connection encounters an error
     */
    private void handle(Socket socket) throws IOException {

        System.err.println("client connected");
        WikiMediator my_wiki = new WikiMediator();

        // get the socket's input stream, and wrap converters around it
        // that convert it from a byte stream to a character stream,
        // and that buffer it so that we can read a line at a time
        BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));

        // similarly, wrap character=>bytestream converter around the
        // socket output stream, and wrap a PrintWriter around that so
        // that we have more convenient ways to write Java primitive
        // types to it.
        PrintWriter out = new PrintWriter(new OutputStreamWriter(
                socket.getOutputStream()), true);

        try {
            JsonParser parser = new JsonParser();
            Gson gson = new Gson();
            // each request is a single line containing
            // id , type , query, limit
            for (String line = in.readLine(); line != null; line = in
                    .readLine()) {
                System.err.println("request: " + line);
                try {
                    JsonElement json = parser.parse(line);
                    if (json.isJsonArray()) {
                        JsonArray questionArray = json.getAsJsonArray();
                        for (int i = 0; i < questionArray.size(); i++) {
                            JsonObject question = questionArray.get(i).getAsJsonObject();
                            String type = question.get("id").getAsString();
                            JsonElement answer;
                            if (type.compareTo("simpleSearch") == 0) {
                                answer = simpleSearchquery(question, my_wiki);
                            }
                            else if(type.compareTo("zeitgeist")==0){
                                answer = zeitgeistquery(question, my_wiki);
                            }
                            else if(type.compareTo("getConnectedPages")==0){
                                answer = getConnectedPagesquery(question, my_wiki);
                            }
                            else if(type.compareTo("trending")==0){
                                answer = zeitgeistquery(question, my_wiki);
                            }
                            else if(type.compareTo("peakLoad30s")==0){
                                answer = peakLoad30squery(question, my_wiki);
                            }
                            else {
                                answer = parser.parse("Request cannot be resolved");
                            }

                            System.out.println(answer);

                        }
                    }
                } catch (Exception e) {
                    // complain about ill-formatted request
                    System.err.println("reply: err");
                    out.print("err\n");
                }
            }
        } finally {
            out.close();
            in.close();
        }

    }


    private JsonElement simpleSearchquery(JsonObject question, WikiMediator my_wiki) {

        String id = question.get("id").getAsString();
        String query = question.get("query").getAsString();
        int limit = question.get("limit").getAsInt();
        List<String> responses = my_wiki.simpleSearch(query, limit);
        boolean success = (responses.size() > 0);
        String status = success ? "success" : "failed";
        JsonObject answer = new JsonObject();
        answer.addProperty("id", id);
        answer.addProperty("status", status);
        String response;
        if (success) {
            response = responses.get(0);
            for (int j = 1; j < responses.size(); j++) {
                response = response.concat("\"," + responses.get(j));
            }
        } else {
            response = "Operation timed out";
        }

        answer.addProperty("response", response);
        return answer;
    }

    private JsonElement zeitgeistquery(JsonObject question, WikiMediator my_wiki) {
        String id = question.get("id").getAsString();
        int limit = question.get("limit").getAsInt();
        List<String> responses = my_wiki.zeitgeist(limit);
        boolean success = (responses.size() > 0);
        String status = success ? "success" : "failed";
        JsonObject answer = new JsonObject();
        answer.addProperty("id", id);
        answer.addProperty("status", status);
        String response;
        if (success) {
            response = responses.get(0);
            for (int j = 1; j < responses.size(); j++) {
                response = response.concat("\"," + responses.get(j));
            }
        } else {
            response = "Operation timed out";
        }

        answer.addProperty("response", response);
        return answer;
    }

    private JsonElement getConnectedPagesquery(JsonObject question, WikiMediator my_wiki) {
        String id = question.get("id").getAsString();
        String pageTitle = question.get("pageTitle").getAsString();
        int hops = question.get("hops").getAsInt();
        int timeout = question.get("timeout").getAsInt();
        long start = System.currentTimeMillis();
        List<String> responses = my_wiki.getConnectedPages(pageTitle, hops);
        long end  = System.currentTimeMillis();
        String status, response;
        if((end-start)/1000>timeout){
            status =  "failed";
        }
        else status = "success";
        JsonObject answer = new JsonObject();
        answer.addProperty("id", id);
        answer.addProperty("status", status);
        if (status.compareTo("success")==0) {
            response = responses.get(0);
            for (int j = 1; j < responses.size(); j++) {
                response = response.concat("\"," + responses.get(j));
            }
        } else {
            response = "Operation timed out";
        }

        answer.addProperty("response", response);
        return answer;
    }

    private JsonObject peakLoad30squery(JsonObject question, WikiMediator my_wiki){
        String id = question.get("id").getAsString();
        int response = my_wiki.peakLoad30s();

        JsonObject answer = new JsonObject();
        answer.addProperty("id", id);
        answer.addProperty("status", "success");
        answer.addProperty("response", response);
        return answer;

    }
}
