package cpen221.mp3.wikimediator;

import cpen221.mp3.cache.Cacheable;

public class OtherString implements Cacheable {
    /**
     * A class that implements Cacheable
     * and used to instantiate Cache
     */
        private String underlyingString;
        private String title;

        public OtherString(String underlyingString) {
            this.underlyingString = underlyingString;
        }

        public OtherString(){
            this.underlyingString = new String();
        }

        public void put_id(String title){
            this.title = title;
        }

        public String id(){
            return this.title;
        }

        public String toString(OtherString pagetext){
            return this.underlyingString;
        }

}
