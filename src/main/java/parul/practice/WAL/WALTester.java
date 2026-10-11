package parul.practice.WAL;

import java.io.IOException;

public class WALTester {
    public static void main(String[] args) throws IOException {

        // ===== RUN 1: Write data, simulate crash =====
        System.out.println("=== RUN 1: Writing data ===");
        WALService service = new WALService();

        service.beginTransaction();
        service.put("Name", "Anthony");
        service.put("Age", "31");
        service.put("Email", "anthony@gmail.com");
        service.commit();

        // Simulate crash — start second transaction but DON'T commit
        service.beginTransaction();
        service.put("Name", "Bob");  // this should NOT survive crash
        service.put("Age", "25");    // this should NOT survive crash
        // NO commit — crash happens here

        System.out.println("Before crash KVStore: " + service.getKVStore().getAll());
        // Should show: {Name=Bob, Age=25, Email=anthony@gmail.com}
        // Bob overwrote Anthony in memory — but WAL has no COMMIT for txn 2

        // ===== RUN 2: Restart and recover =====
        System.out.println("\n=== RUN 2: Recovery after crash ===");
        WALService recovered = new WALService();
        recovered.startup();  // replay WAL

        System.out.println("After recovery KVStore: " + recovered.getKVStore().getAll());
        // Should show: {Name=Anthony, Age=31, Email=anthony@gmail.com}
        // Bob's changes discarded — txn 2 never committed


    }
}
