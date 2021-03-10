package cpen221.mp3.wikimediator;

import cpen221.mp3.cache.Cache;
import fastily.jwiki.core.Wiki;

import java.util.*;

/**
 * AF : a mediator service for wikipedia that can handle basic
 *       page requests and other queries
 * RI : the wikipedia webpages queried must exist in form of links.
 */

public class WikiMediator {

    /* TODO: Implement this datatype

        You must implement the methods with the exact signatures
        as provided in the statement for this mini-project.

        You must add method signatures even for the methods that you
        do not plan to implement. You should provide skeleton implementation
        for those methods, and the skeleton implementation could return
        values like null.

     */


    private Map<String, Integer> frequency_map;
    private Map<String, Long> time_map;
    private List<Long> requests;
    private Wiki my_wiki;
    private Cache mycache;


    /**
     * constructor to initialize a service between Wikipedia and the user.
     *
     */

    public WikiMediator() {
        frequency_map = new HashMap<String, Integer>();
        my_wiki = new Wiki("en.wikipedia.org");
        time_map = new HashMap<String,Long>();
        //my_wiki.enableLogging(false);
        mycache = new Cache<OtherString>(256, 43200);
        requests = new ArrayList<>();
    }
    /**
     *
     *Given a query, return up to limit page titles that match the
     *query string (per Wikipedia's search service).
     * @param query the string to match
     * @param limit the max no. of page titles to return
     * @return list of all pages with that match the query
     */
    public List<String> simpleSearch(String query, int limit){

        if(frequency_map.containsKey(query)){
            int num = frequency_map.get(query);
            frequency_map.put(query,num+1);
        }
        else frequency_map.put(query,1);

        time_map.put(query, System.currentTimeMillis()/1000);

        requests.add(System.currentTimeMillis()/1000);

        OtherString Myquery = new OtherString(my_wiki.getPageText(query));
        Myquery.put_id(query);
        mycache.put(Myquery);

        return my_wiki.allPages(query, false, false, limit, null);
    }


    /**
     *
     *Given a pageTitle, return the text associated with the
     *Wikipedia page that matches pageTitle.
     * @param pageTitle the title of the page to look for
     * @return the text associated with the page
     *
     */
    @SuppressWarnings("unchecked")
    public String getPage(String pageTitle){

        if(frequency_map.containsKey(pageTitle)){
            int num = frequency_map.get(pageTitle);
            frequency_map.put(pageTitle,num+1);
        }
        else frequency_map.put(pageTitle,1);

        time_map.put(pageTitle, System.currentTimeMillis()/1000);

        requests.add(System.currentTimeMillis()/1000);

        Thread t = new Thread(mycache);
        t.start();
        try {
            t.join();
        } catch(Exception e){
            System.out.println("InterruptedException by join");
        }

        OtherString mypage = new OtherString();
        mypage.put_id(pageTitle);

        try{
            mypage = (OtherString)mycache.get(mypage.id());
            return mypage.toString();
        } catch(Exception e){
            mypage = new OtherString(my_wiki.getPageText(pageTitle));
            mypage.put_id(pageTitle);
            boolean done = mycache.put(mypage);
            if(!done) mycache.put(mypage);
        }

        return mypage.toString();
    }

    /*
     * Return a list of page titles that can be reached by following up to hops links
     * starting with the page specified by pageTitle
     * @param pageTitle Title string of the starting Wikipedia page
     * @param hops maximum number of links that can be followed
     * @return List of Strings that can be reached by following a maximum of hops links from pageTitle
     */

   public List<String> getConnectedPages(String pageTitle, int hops) {

        requests.add(System.currentTimeMillis()/1000);      //count number # of function called

        int hop=0;
        List<String> sublinks = linkToTitle(my_wiki.getLinksOnPage(pageTitle));
        List<String> result = sublinks;

        while(hop<hops){

            List<String> pages = new ArrayList<>();

            for(int i=0; i<sublinks.size(); i++){

                try {
                    List<String> links = linkToTitle(my_wiki.getLinksOnPage(true,sublinks.get(i)));
                    result.addAll(links);
                    pages.addAll(links);
                }
                catch (Exception e){
                    pages = new ArrayList<String>();
                }
            }
            sublinks = pages;

            hop++;
        }

        return result;

   }

    /**
     * Converts links to titles
     * @param pageLinks list of links that need to be converted to titles
     * @return a list of page titles according to the page links
     */
    private List<String> linkToTitle (List<String> pageLinks){
        List<String> page_titles = new ArrayList<>();
        StringBuilder title;

        for(int i = 0; i<pageLinks.size(); i++) {
            StringBuilder link = new StringBuilder(pageLinks.get(i));
            title = link.delete(0, 21);
            while(title.toString().contains("_")){
                title.replace(title.indexOf("_"),title.indexOf("_")," ");
            }
            page_titles.add(title.toString());
        }
        return page_titles;
    }



    /**
     *precondition: limit>=0
     *Return the most common Strings used in simpleSearch and getPage requests,
     *with items being sorted in non-increasing count order.
     *When many requests have been made, return only limit items.
     * @param limit the max no. of strings to return
     * @return a list of common strings used in simpleSearch and getPage,
     *          in non-increasing order
     *
     */
    public List<String> zeitgeist(int limit){

        requests.add(System.currentTimeMillis()/1000);

        ArrayList<String> keyList = new ArrayList<String>(frequency_map.keySet());
        ArrayList<Integer> countList = new ArrayList<Integer>(frequency_map.values());
        List<String> commons = new ArrayList<>();

        Collections.sort(countList, Collections.reverseOrder());

        for (int i = 0; i < countList.size()&&commons.size()<limit; i++) {
            int count = countList.get(i);
            for (int j = 0; j < keyList.size() && commons.size()<=limit; j++) {
                String string = keyList.get(i);

                if (frequency_map.get(string) == count && !commons.contains(string) && commons.size() < limit) {
                    commons.add(string);
                }
            }
        }

        return commons;
    }


    /**
     *precondition: limit>=0
     *Similar to zeitgeist(), but returns the most frequent requests
     *made in the last 30 seconds.
     * @param limit the max no. of strings to return
     * @return the most frequent requests made in the last 30 sec.
     */
    public List<String> trending(int limit) {

        requests.add(System.currentTimeMillis()/1000);

        ArrayList<Long> times = new ArrayList<>(time_map.values());
        Collections.sort(times, Collections.reverseOrder());
        List<String> freq = new ArrayList<>();
        Long current = System.currentTimeMillis()/1000;

        for(Long t: times) {
            if(current-t > 30){
                break;
            }
            for (String s : time_map.keySet()) {
                if(time_map.get(s).equals(t) && freq.size()<limit){
                    freq.add(s);
                }
            }
        }
        return freq;
    }


    /*
     * Returns maximum number of requests seen in any 30-second window
     * @return maximum number of search requests seen in any 30-second window
     *
     */
    public int peakLoad30s(){
        Long current = System.currentTimeMillis()/1000;
        requests.add(current);
        int count = 0;
        for(int j=0;j<requests.size();j++){
            if(current-requests.get(j)<=30){
                count++;
            }
        }
        return count;
    }


    /*
     * Returns path of links between two Wikipedia pages
     * @param startPage String of name of the starting Wikipedia page
     * @param stopPage String of the name of the ending Wikipedia page
     * @return List of Strings containing the links to follow to get from
     *         startPage to stopPage
     */
    List<String> getPath(String startPage, String stopPage){
       requests.add(System.currentTimeMillis()/1000);      //count number # of function called

          List<String> sublinks = my_wiki.getLinksOnPage(startPage);
          List<String> result = sublinks;
          List<String> pages = new ArrayList<>();
                  for (int i = 0; i < sublinks.size(); i++) {
                      if(result.contains(stopPage))
                          return result;
                      try {
                          List<String> links = my_wiki.getLinksOnPage(true, sublinks.get(i));
                          result.addAll(links);
                          pages.addAll(links);
                      } catch (Exception e) {
                          pages = new ArrayList<String>();
                      }
                      sublinks = pages;
                  }
          return result;
    }

    /*
     * Returns a list of pages that meet the structured query from the user
     * @param query String representing the structured query
     * @return a List of Strings containing the page titles meeting the requirements from query
     */
    List<String> executeQuery(String query){
        return null;
    }


}
