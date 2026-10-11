package parul.practice.WAL;

import java.io.IOException;

public class SingleNodeKVStore {

    WALService service;
    public SingleNodeKVStore() throws IOException {
        service = new WALService();
        service.startup();

    }

    public void put(String key, String value) throws  IOException{
        service.beginTransaction();
        service.put(key, value);
        service.commit();
    }

    public void delete(String key) throws IOException {
        service.beginTransaction();
        service.delete(key);
        service.commit();
    }

    public String get(String key){
        return service.get(key);
    }
}
