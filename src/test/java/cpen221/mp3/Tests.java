package cpen221.mp3;

import com.google.gson.JsonObject;
import cpen221.mp3.cache.Cache;
import cpen221.mp3.server.WikiMediatorServer;
import cpen221.mp3.wikimediator.OtherString;
import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.Test;

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.util.List;

public class Tests {

    /*
        You can add your tests here.
        Remember to import the packages that you need, such
        as cpen221.mp3.cache.
     */
    @Test
    public void test1()
    {
        WikiMediator w=new WikiMediator();
        List<String> result=w.simpleSearch("Barack Obama", 12);
        w.getPage("Barack Obama");
        System.out.println(result);
    }

    @Test
    public void test2()
    {
        WikiMediator w=new WikiMediator();
        List<String> result=w.getConnectedPages("Hausi A. Muller", 1);
        System.out.println(result.size());
    }

    @Test
    public void test3()
    {
        WikiMediator w=new WikiMediator();
        w.getPage("Disney");
        w.simpleSearch("Malware", 2);
        w.getPage("Malware");
        w.getPage("Knowledge");
        w.simpleSearch("Mango", 0);
        List<String> result=w.zeitgeist(3);
        for(int i=0;i<result.size();i++)
        {
            System.out.println(result.get(i));
        }
    }

    @Test
    public void test4()
    {
        WikiMediator w=new WikiMediator();
        w.simpleSearch("Malware", 2);
        w.simpleSearch("Mango", 1);
        w.simpleSearch("Malware", 3);
        w.simpleSearch("Malware", 4);
        w.simpleSearch("Malware", 2);
        w.simpleSearch("Malware", 1);
        w.simpleSearch("Mango", 2);
        w.simpleSearch("Random", 1);
        w.simpleSearch("Malware", 3);
        w.simpleSearch("Malware", 4);
        List<String> result=w.trending(5);
        for(int i=0;i<result.size();i++)
        {
            System.out.println(result.get(i));
        }
    }
    @Test
    public void test5()
    {
        WikiMediator w=new WikiMediator();
        w.simpleSearch("Malware", 2);
        w.getPage("Mango");
        w.getConnectedPages("Random", 5);
        w.trending(2);
        w.zeitgeist(3);
        System.out.println(w.peakLoad30s());
    }

    @Test
    public void test7() throws NoSuchObjectException {
        Cache mycache = new Cache<OtherString>(256, 43200);
        WikiMediator w=new WikiMediator();
        OtherString ostr1=new OtherString(w.getPage("Hello"));
        OtherString ostr2=new OtherString(w.getPage("World"));
        OtherString ostr3=new OtherString(w.getPage("Good"));
        OtherString ostr4=new OtherString();
        ostr4.put_id("Morning");
        mycache.put(ostr1);
        mycache.put(ostr2);
        mycache.put(ostr3);
        mycache.put(ostr4);

        OtherString mypage1=new OtherString();
        OtherString mypage2=new OtherString();
        OtherString mypage3=new OtherString();
        OtherString mypage4=new OtherString();
        try{
            mypage1 = (OtherString)mycache.get(mypage1.id());
            mypage2 = (OtherString)mycache.get(mypage2.id());
            mypage3 = (OtherString)mycache.get(mypage3.id());
            mypage4 = (OtherString)mycache.get(mypage4.id());
            System.out.println(mypage1.toString());
            System.out.println(mypage2.toString());
            System.out.println(mypage3.toString());
            System.out.println(mypage4.toString());
        }
        catch(Exception e){
            mypage1 = new OtherString("Hello");
            mypage2 = new OtherString("World");
            mypage3 = new OtherString("Good");
            mypage4 = new OtherString("Morning");
            boolean done1 = mycache.put(mypage1);
            boolean done2 = mycache.put(mypage2);
            boolean done3 = mycache.put(mypage3);
            boolean done4 = mycache.put(mypage4);
            if(!done1) mycache.put(mypage1);
            if(!done2) mycache.put(mypage2);
            if(!done3) mycache.put(mypage3);
            if(!done4) mycache.put(mypage4);
        }

    }

    @Test
    public void test8(){
        try {
            WikiMediatorServer server = new WikiMediatorServer(4949, 2);
            server.serve();
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
            JsonObject answer = client.getReply();
            System.out.println(answer);
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test9(){
        try {
            WikiMediatorServer server = new WikiMediatorServer(4949, 2);
            server.serve();
            WikiMediatorClient client = new WikiMediatorClient("localhost", 4949);

            // send the requests

            JsonObject j2 = new JsonObject();
            j2.addProperty("id","two");
            j2.addProperty("type", "zeitgeist");
            j2.addProperty("limit", 5);
            client.sendRequest(j2);
            System.out.println("zeitgeist");

            // collect the replies
            JsonObject y = client.getReply();
            System.out.println(y);

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test10(){
        try {
            WikiMediatorServer server = new WikiMediatorServer(4949, 2);
            server.serve();
            WikiMediatorClient client = new WikiMediatorClient("localhost", 4949);

            // send the requests
            JsonObject j3 = new JsonObject();
            j3.addProperty("id","3");
            j3.addProperty("type", "getConnectedPages");
            j3.addProperty("pageTitle", "Barack Obama");
            j3.addProperty("hops", "12");
            j3.addProperty("timeout", "2");
            client.sendRequest(j3);
            System.out.println("getConnectedPages");

            // collect the replies
            JsonObject y = client.getReply();
            System.out.println(y);

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test11(){
        try {
            WikiMediatorServer server = new WikiMediatorServer(4949, 2);
            server.serve();
            WikiMediatorClient client = new WikiMediatorClient("localhost", 4949);

            // send the requests
            JsonObject j4 = new JsonObject();
            j4.addProperty("id","3");
            j4.addProperty("status", "failed");
            j4.addProperty("response", "Operation timed out");
            client.sendRequest(j4);
            System.out.println("Operation timed out");

            // collect the replies
            JsonObject y = client.getReply();
            System.out.println(y);

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void test12()
   {
       WikiMediator w=new WikiMediator();
       List<String> result=w.getPath("a", "English");
       System.out.println(result.size());
       for(int i=0;i<result.size();i++)
       {
           System.out.println(result.get(i));
       }
   }

}
