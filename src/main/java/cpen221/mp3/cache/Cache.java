package cpen221.mp3.cache;

import java.rmi.NoSuchObjectException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * RI: Cache represents locally stored objects upto the "capacity" count.
 * It stores these objects for the time period "timeout".
 *
 * AF: The objects that have reached
 * this timeout are removed.
 *
 */

public class Cache<T extends Cacheable> implements Runnable {

    /* the default cache size is 32 objects */
    public static final int DSIZE = 32;

    /* the default timeout value is 3600s */
    public static final int DTIMEOUT = 3600;

    /* TODO: Implement this datatype */

    private int capacity;
    private int timeout;
    private Map<T, Date> cache;

    /**
     * Create a cache with a fixed capacity and a timeout value.
     * Objects in the cache that have not been refreshed within the timeout period
     * are removed from the cache.
     *
     * @param capacity the number of objects the cache can hold
     * @param timeout  the duration an object should be in the cache before it times out
     */
    public Cache(int capacity, int timeout) {
        // TODO: implement this constructor
        this.capacity = capacity;
        this.timeout = timeout;
        this.cache = new HashMap<>(capacity);
        this.run();

    }

    /**
     * Create a cache with default capacity and timeout values.
     */
    public Cache() {
        this(DSIZE, DTIMEOUT);
        this.run();
    }

    /**
     * Add a value to the cache.
     * If the cache is full then remove the least recently accessed object to
     * make room for the new object.
     * @param t any generic object you want to add to the cache
     * @return true if the value was added succesfully,
     *           false if the value was not added(no space)
     * @modifies removes an object from cache to make room for the new object
     */
    synchronized public boolean put(T t) {
        // TODO: implement this method
        Date date = new Date(System.currentTimeMillis());
        if(cache.size() < capacity){
            cache.put(t, date);
            return true;
        } else {
            Date least_recent_date = date;
            T least_recent_object = null;
            for(Map.Entry<T,Date> entry : cache.entrySet()){
                if(entry.getValue().before(least_recent_date)){
                    least_recent_date = entry.getValue();
                    least_recent_object = entry.getKey();
                }
            }

            cache.remove(least_recent_object);

        }
        return false;
    }

    /**
     * @param id the identifier of the object to be retrieved
     * @return the object that matches the identifier from the cache
     * @throws NoSuchObjectException when an object with id does not exist
     */
    synchronized public T get(String id) throws NoSuchObjectException{
        /* TODO: change this */
        /* Do not return null. Throw a suitable checked exception when an object
            is not in the cache. */

        for(T t: cache.keySet()){
            if(t.id().equals(id)){
                return t;
            }
        }
        throw new NoSuchObjectException("no such object in the cache");
    }

    /**
     * Update the last refresh time for the object with the provided id.
     * This method is used to mark an object as "not stale" so that its timeout
     * is delayed.
     *
     * @param id the identifier of the object to "touch"
     * @return true if successful and false otherwise
     */
    synchronized public boolean touch(String id) {
        /* TODO: Implement this method */

        Date refresh_time = new Date(System.currentTimeMillis());
        for(T t: cache.keySet()){
            if(t.id().equals(id)){
                cache.put(t, refresh_time);
                return true;
            }
        }
        return false;
    }

    /**
     * Update an object in the cache.
     * This method updates an object and acts like a "touch" to renew the
     * object in the cache.
     *
     * @param t the object to update
     * @return true if successful and false otherwise
     */
    synchronized public boolean update(T t) {
        /* TODO: implement this method */
        Date time = new Date(System.currentTimeMillis());
        for(T t1: cache.keySet()){
            if(t1.equals(t)){
                cache.put(t1, time);
                return true;
            }
        }
        return false;
    }

    /**
     * a thread that removes an object when it has been in the cache
     * for longer "timeout" period
     */
    public void run(){
        Date current = new Date(System.currentTimeMillis());
        for(T t: cache.keySet()){
            long diff = current.getTime()-cache.get(t).getTime();
            if(diff>=timeout*1000){
                cache.remove(t);
            }
        }
    }

}
