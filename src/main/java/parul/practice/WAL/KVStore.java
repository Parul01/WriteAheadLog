package parul.practice.WAL;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KVStore {

    Map<String, String> DB;

    public KVStore(){
        DB = new ConcurrentHashMap<>();
    }

    public void put(String key,String val){
        DB.put(key,val);
    }


    public void remove(String key){
        DB.remove(key);
    }

    public String get(String key){
        return DB.get(key);
    }

    // use for testing
    public Map<String, String> getAll(){
        return Collections.unmodifiableMap(DB);
    }


}
